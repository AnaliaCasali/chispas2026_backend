package edu.ies63.chispas2026_backend.service.impl;

import edu.ies63.chispas2026_backend.dto.UsuarioResponseDTO;
import edu.ies63.chispas2026_backend.exception.PasswordInvalidaException;
import edu.ies63.chispas2026_backend.exception.UsuarioNoEncontradoException;
import edu.ies63.chispas2026_backend.model.Usuario;
import edu.ies63.chispas2026_backend.repository.UsuarioRepository;
import edu.ies63.chispas2026_backend.security.dto.CambiarPasswordRequest;
import edu.ies63.chispas2026_backend.service.UsuarioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.Principal;
import java.util.List;

@Service
@Slf4j
public class UsuarioServiceImpl implements UsuarioService, UserDetailsService {
  private final UsuarioRepository usuarioRepository;
  private final PasswordEncoder passwordEncoder;

  public UsuarioServiceImpl(UsuarioRepository usuarioRepository, @Lazy PasswordEncoder passwordEncoder) {
    this.usuarioRepository = usuarioRepository;
      this.passwordEncoder = passwordEncoder;
  }

  private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
    return new UsuarioResponseDTO(
        usuario.getId(),
        usuario.getUsername(),
        usuario.getRole(),
        usuario.getNombre());
  }




  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return usuarioRepository.findByUsername(username)
        .orElseThrow(() ->
            new UsernameNotFoundException("Usuario no encontrado: " + username));
  }

  @Override
  @Transactional
  public void cambiarPassword(CambiarPasswordRequest request, Principal connectedUser) {
    String username = connectedUser.getName();

    Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new UsuarioNoEncontradoException("No se encontró el usuario: " + username));

    if (request.newPassword() == null || request.newPassword().length() < 8) {
      throw new PasswordInvalidaException("La nueva contraseña debe tener al menos 8 caracteres");
    }

    if (!passwordEncoder.matches(request.currentPassword(), usuario.getPassword())) {
      throw new PasswordInvalidaException("La contraseña actual es incorrecta");
    }

    if (!request.newPassword().equals(request.confirmationPassword())) {
      throw new PasswordInvalidaException("Las nuevas contraseñas no coinciden");
    }

    if (passwordEncoder.matches(request.newPassword(), usuario.getPassword())) {
      throw new PasswordInvalidaException("La nueva contraseña no puede ser igual a la anterior");
    }

    usuario.setPassword(passwordEncoder.encode(request.newPassword()));
    usuarioRepository.save(usuario);
  }
}
