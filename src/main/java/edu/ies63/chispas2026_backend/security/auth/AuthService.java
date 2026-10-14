package edu.ies63.chispas2026_backend.security.auth;

import edu.ies63.chispas2026_backend.dtos.UsuarioCreateDTO;
import edu.ies63.chispas2026_backend.enums.Role;
import edu.ies63.chispas2026_backend.exceptions.EmailYaRegistradoException;
import edu.ies63.chispas2026_backend.models.PasswordResetToken;
import edu.ies63.chispas2026_backend.models.Usuario;
import edu.ies63.chispas2026_backend.repositories.PasswordResetTokenRepository;
import edu.ies63.chispas2026_backend.repositories.UsuarioRepository;
import edu.ies63.chispas2026_backend.security.dto.AuthRequest;
import edu.ies63.chispas2026_backend.security.dto.AuthResponse;
import edu.ies63.chispas2026_backend.security.dto.ForgotPasswordRequest;
import edu.ies63.chispas2026_backend.security.dto.ResetPasswordRequest;
import edu.ies63.chispas2026_backend.security.jwt.JwtService;
import edu.ies63.chispas2026_backend.services.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

  private final UsuarioRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final PasswordResetTokenRepository resetTokenRepository;
  private final EmailService emailService;


  public AuthResponse authenticate(@Valid AuthRequest request) {
    Usuario user = userRepository.findByUsernameOrDni(request.username(), request.username())
          .orElseThrow(() -> new ResponseStatusException(
              HttpStatus.UNAUTHORIZED,
              "Credenciales inválidas"));
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            user.getUsername(),
            request.password()
        )
    );
    return guardarYGenerarToken(user);
  }

  @Transactional
  public AuthResponse registerUsuario(UsuarioCreateDTO request) {
    validarUsernameDisponible(request.username());

    Usuario user = Usuario.builder()
        .nombre(request.nombre())
        .username(request.username())
        .password(passwordEncoder.encode(request.password()))
        .role(Role.USUARIO)          // ← rol FIJO, no viene del cliente
        .debaja(false)
        .build();

    return guardarYGenerarToken(user);
  }



  // métodos privados para reutilizar

  private AuthResponse guardarYGenerarToken(Usuario user) {
    userRepository.save(user);

    Map<String, Object> claims = new HashMap<>();
    claims.put("rol", user.getRole().name());
    claims.put("id", user.getId());
    claims.put("name", user.getNombre());

    return new AuthResponse(jwtService.generateToken(claims, user));
  }

  private void validarUsernameDisponible(String username) {
    if (userRepository.findByUsername(username).isPresent()) {
      throw new EmailYaRegistradoException("El usuario ya existe: " + username);
    }
  }


  @Transactional
  public void requestPasswordReset(ForgotPasswordRequest request) {
    Usuario user = userRepository.findByUsername(request.email())
            .or(() -> userRepository.findByUsername(request.email()))
            .orElse(null);

    if (user == null) {
      return;
    }

    // 1. Limpiar tokens anteriores asignados a este usuario
    resetTokenRepository.deleteByUsuario(user);

    // 2. Generar token único con expiración de 15 minutos
    String token = UUID.randomUUID().toString();
    PasswordResetToken resetToken = PasswordResetToken.builder()
            .token(token)
            .usuario(user)
            .expiryDate(LocalDateTime.now().plusMinutes(15))
            .build();

    resetTokenRepository.save(resetToken);

    // 3. Enviar correo electrónico
    emailService.sendPasswordResetEmail(user.getUsername(), token);
  }

  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    if (request.newPassword() == null || request.newPassword().length() < 8) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nueva contraseña debe tener al menos 8 caracteres");
    }

    if (!request.newPassword().equals(request.confirmationPassword())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las contraseñas no coinciden");
    }

    PasswordResetToken resetToken = resetTokenRepository.findByToken(request.token())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token de recuperación inválido o inexistente"));

    if (resetToken.isExpired()) {
      resetTokenRepository.delete(resetToken);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El token de recuperación ha expirado");
    }

    // Actualizar contraseña con hash BCrypt
    Usuario user = resetToken.getUsuario();
    user.setPassword(passwordEncoder.encode(request.newPassword()));
    userRepository.save(user);

    // Eliminar token para que no pueda reutilizarse
    resetTokenRepository.delete(resetToken);
  }
}
