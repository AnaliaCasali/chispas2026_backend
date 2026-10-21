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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// tests unitarios de AuthService: login, registro y recuperacion de contraseña con repositorios mockeados
class AuthServiceTest {

  private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
  private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
  private final JwtService jwtService = mock(JwtService.class);
  private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
  private final PasswordResetTokenRepository resetTokenRepository = mock(PasswordResetTokenRepository.class);
  private final EmailService emailService = mock(EmailService.class);

  private final AuthService authService = new AuthService(usuarioRepository, passwordEncoder, jwtService,
      authenticationManager, resetTokenRepository, emailService);

  private final Usuario laura = Usuario.builder().id(1L).dni("20111222").email("docente@chispas.edu.ar")
      .password("hash-viejo").nombre("Laura").apellido("Gómez").role(Role.DOCENTE).activo(true).build();

  private UsuarioCreateDTO nuevoAlumno(String email) {
    return new UsuarioCreateDTO(email, "Chispas2026", "Ana", "Pérez", "40111222", "https://img/ana.jpg");
  }

  private ResetPasswordRequest reset(String nueva, String confirmacion) {
    return new ResetPasswordRequest("token-123", nueva, confirmacion);
  }

  private PasswordResetToken tokenQueVence(LocalDateTime vencimiento) {
    return PasswordResetToken.builder().token("token-123").usuario(laura).expiryDate(vencimiento).build();
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> claimsDelToken() {
    ArgumentCaptor<Map<String, Object>> claims = ArgumentCaptor.forClass(Map.class);
    verify(jwtService).generateToken(claims.capture(), any());
    return claims.getValue();
  }

  // ---------- login ----------

  // login correcto: autentica con el username del usuario y el token lleva rol, id y nombre
  @Test
  void login_conCredencialesValidas_devuelveTokenConLosDatosDelUsuario() {
    when(usuarioRepository.findByEmailOrDni("docente@chispas.edu.ar", "docente@chispas.edu.ar"))
        .thenReturn(Optional.of(laura));
    when(jwtService.generateToken(anyMap(), eq(laura))).thenReturn("jwt");

    AuthResponse respuesta = authService.authenticate(new AuthRequest("docente@chispas.edu.ar", "Chispas2026"));

    assertThat(respuesta.token()).isEqualTo("jwt");
    verify(authenticationManager).authenticate(
        new UsernamePasswordAuthenticationToken("docente@chispas.edu.ar", "Chispas2026"));
    assertThat(claimsDelToken()).containsEntry("rol", "DOCENTE").containsEntry("id", 1L).containsEntry("name", "Laura");
  }

  // usuario que no existe: 401 sin llegar a autenticar
  @Test
  void login_usuarioInexistente_devuelve401() {
    when(usuarioRepository.findByEmailOrDni(anyString(), anyString())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.authenticate(new AuthRequest("nadie@chispas.edu.ar", "x")))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED))
        .hasMessageContaining("Credenciales inválidas");
    verifyNoInteractions(authenticationManager, jwtService);
  }

  // contraseña incorrecta: la excepcion del AuthenticationManager sube y no se genera token
  @Test
  void login_passwordIncorrecta_noGeneraToken() {
    when(usuarioRepository.findByEmailOrDni(anyString(), anyString())).thenReturn(Optional.of(laura));
    when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("mal"));

    assertThatThrownBy(() -> authService.authenticate(new AuthRequest("docente@chispas.edu.ar", "otra")))
        .isInstanceOf(BadCredentialsException.class);
    verifyNoInteractions(jwtService);
    verify(usuarioRepository, never()).save(any());
  }

  // ---------- registro ----------

  // registro: guarda el usuario como ESTUDIANTE activo con la contraseña encriptada
  @Test
  void registro_guardaEstudianteActivoConPasswordEncriptada() {
    when(usuarioRepository.findByEmail("ana@chispas.edu.ar")).thenReturn(Optional.empty());
    when(usuarioRepository.findByDni("40111222")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("Chispas2026")).thenReturn("hash");
    when(jwtService.generateToken(anyMap(), any())).thenReturn("jwt");

    AuthResponse respuesta = authService.registerUsuario(nuevoAlumno("ana@chispas.edu.ar"));

    assertThat(respuesta.token()).isEqualTo("jwt");
    ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
    verify(usuarioRepository).save(guardado.capture());
    Usuario ana = guardado.getValue();
    assertThat(ana.getRole()).isEqualTo(Role.ESTUDIANTE);
    assertThat(ana.isActivo()).isTrue();
    assertThat(ana.getPassword()).isEqualTo("hash");
    assertThat(ana.getNombre()).isEqualTo("Ana");
    assertThat(ana.getApellido()).isEqualTo("Pérez");
    assertThat(ana.getDni()).isEqualTo("40111222");
    assertThat(ana.getEmail()).isEqualTo("ana@chispas.edu.ar");
    assertThat(ana.getFotoUrl()).isEqualTo("https://img/ana.jpg");
    assertThat(claimsDelToken()).containsEntry("rol", "ESTUDIANTE").containsEntry("name", "Ana");
  }

  // registro sin email (alumno que entra con DNI): no consulta el email
  @Test
  void registro_sinEmail_noBuscaPorEmail() {
    when(usuarioRepository.findByDni("40111222")).thenReturn(Optional.empty());
    when(jwtService.generateToken(anyMap(), any())).thenReturn("jwt");

    authService.registerUsuario(nuevoAlumno(null));

    verify(usuarioRepository, never()).findByEmail(any());
    verify(usuarioRepository).save(any());
  }

  // email ya registrado: no guarda nada
  @Test
  void registro_emailRepetido_lanzaEmailYaRegistrado() {
    when(usuarioRepository.findByEmail("docente@chispas.edu.ar")).thenReturn(Optional.of(laura));

    assertThatThrownBy(() -> authService.registerUsuario(nuevoAlumno("docente@chispas.edu.ar")))
        .isInstanceOf(EmailYaRegistradoException.class)
        .hasMessage("El email ya está registrado: docente@chispas.edu.ar");
    verify(usuarioRepository, never()).save(any());
  }

  // ---------- olvide mi contraseña ----------

  // email que no existe: no hace nada (no se revela si el email esta registrado)
  @Test
  void olvidePassword_emailInexistente_noHaceNada() {
    when(usuarioRepository.findByEmail("nadie@chispas.edu.ar")).thenReturn(Optional.empty());

    authService.requestPasswordReset(new ForgotPasswordRequest("nadie@chispas.edu.ar"));

    verifyNoInteractions(resetTokenRepository, emailService);
  }

  // usuario sin email cargado: no puede recibir el correo
  @Test
  void olvidePassword_usuarioSinEmail_noHaceNada() {
    Usuario sinEmail = Usuario.builder().id(2L).dni("45123456").build();
    when(usuarioRepository.findByEmail(any())).thenReturn(Optional.of(sinEmail));

    authService.requestPasswordReset(new ForgotPasswordRequest("x@chispas.edu.ar"));

    verifyNoInteractions(resetTokenRepository, emailService);
  }

  // usuario con email: borra los tokens anteriores, guarda uno nuevo de 15 minutos y manda el correo
  @Test
  void olvidePassword_guardaTokenDe15MinutosYMandaElCorreo() {
    when(usuarioRepository.findByEmail("docente@chispas.edu.ar")).thenReturn(Optional.of(laura));
    LocalDateTime antes = LocalDateTime.now();

    authService.requestPasswordReset(new ForgotPasswordRequest("docente@chispas.edu.ar"));

    verify(resetTokenRepository).deleteByUsuario(laura);
    ArgumentCaptor<PasswordResetToken> guardado = ArgumentCaptor.forClass(PasswordResetToken.class);
    verify(resetTokenRepository).save(guardado.capture());
    PasswordResetToken token = guardado.getValue();
    assertThat(token.getUsuario()).isSameAs(laura);
    assertThat(token.getToken()).isNotBlank();
    assertThat(token.getExpiryDate()).isBetween(antes.plusMinutes(15), LocalDateTime.now().plusMinutes(15));
    verify(emailService).sendPasswordResetEmail("docente@chispas.edu.ar", token.getToken());
  }

  // ---------- reset de contraseña ----------

  @Test
  void resetPassword_nuevaMuyCorta_lanza400() {
    assertThatThrownBy(() -> authService.resetPassword(reset("corta", "corta")))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("al menos 8 caracteres");
    verifyNoInteractions(resetTokenRepository);
  }

  @Test
  void resetPassword_nuevaNula_lanza400() {
    assertThatThrownBy(() -> authService.resetPassword(reset(null, null)))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("al menos 8 caracteres");
  }

  @Test
  void resetPassword_confirmacionDistinta_lanza400() {
    assertThatThrownBy(() -> authService.resetPassword(reset("NuevaClave1", "OtraClave1")))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Las contraseñas no coinciden");
    verifyNoInteractions(resetTokenRepository);
  }

  @Test
  void resetPassword_tokenInexistente_lanza400() {
    when(resetTokenRepository.findByToken("token-123")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.resetPassword(reset("NuevaClave1", "NuevaClave1")))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST))
        .hasMessageContaining("Token de recuperación inválido");
  }

  // token vencido: se borra y no cambia la contraseña
  @Test
  void resetPassword_tokenVencido_loBorraYNoCambiaLaPassword() {
    PasswordResetToken vencido = tokenQueVence(LocalDateTime.now().minusMinutes(1));
    when(resetTokenRepository.findByToken("token-123")).thenReturn(Optional.of(vencido));

    assertThatThrownBy(() -> authService.resetPassword(reset("NuevaClave1", "NuevaClave1")))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("ha expirado");
    verify(resetTokenRepository).delete(vencido);
    verify(usuarioRepository, never()).save(any());
    assertThat(laura.getPassword()).isEqualTo("hash-viejo");
  }

  // token vigente: guarda la nueva contraseña encriptada y borra el token para que no se reuse
  @Test
  void resetPassword_tokenVigente_cambiaLaPasswordYBorraElToken() {
    PasswordResetToken vigente = tokenQueVence(LocalDateTime.now().plusMinutes(10));
    when(resetTokenRepository.findByToken("token-123")).thenReturn(Optional.of(vigente));
    when(passwordEncoder.encode("NuevaClave1")).thenReturn("hash-nuevo");

    authService.resetPassword(reset("NuevaClave1", "NuevaClave1"));

    assertThat(laura.getPassword()).isEqualTo("hash-nuevo");
    verify(usuarioRepository).save(laura);
    verify(resetTokenRepository).delete(vigente);
  }
}
