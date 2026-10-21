package edu.ies63.chispas2026_backend.services.impl;

import edu.ies63.chispas2026_backend.exceptions.PasswordInvalidaException;
import edu.ies63.chispas2026_backend.exceptions.UsuarioNoEncontradoException;
import edu.ies63.chispas2026_backend.models.Usuario;
import edu.ies63.chispas2026_backend.repositories.UsuarioRepository;
import edu.ies63.chispas2026_backend.security.dto.CambiarPasswordRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Principal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// tests unitarios de UsuarioServiceImpl: busqueda del usuario para Security y cambio de contraseña
class UsuarioServiceImplTest {

  private static final String EMAIL = "docente@chispas.edu.ar";

  private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
  private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
  private final UsuarioServiceImpl usuarioService = new UsuarioServiceImpl(usuarioRepository, passwordEncoder);

  private final Principal conectado = () -> EMAIL;
  private Usuario laura;

  @BeforeEach
  void setUp() {
    laura = Usuario.builder().id(1L).email(EMAIL).password("hash-actual").build();
    when(usuarioRepository.findByEmailOrDni(EMAIL, EMAIL)).thenReturn(Optional.of(laura));
    when(passwordEncoder.matches("Actual123", "hash-actual")).thenReturn(true);
  }

  private void cambiar(String actual, String nueva, String confirmacion) {
    usuarioService.cambiarPassword(new CambiarPasswordRequest(actual, nueva, confirmacion), conectado);
  }

  // ---------- loadUserByUsername ----------

  @Test
  void loadUserByUsername_devuelveElUsuario() {
    assertThat(usuarioService.loadUserByUsername(EMAIL)).isSameAs(laura);
  }

  @Test
  void loadUserByUsername_inexistente_lanzaUsernameNotFound() {
    when(usuarioRepository.findByEmailOrDni(anyString(), anyString())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> usuarioService.loadUserByUsername("nadie"))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessage("Usuario no encontrado: nadie");
  }

  // ---------- cambiarPassword ----------

  @Test
  void cambiarPassword_usuarioInexistente_lanzaUsuarioNoEncontrado() {
    when(usuarioRepository.findByEmailOrDni(EMAIL, EMAIL)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> cambiar("Actual123", "NuevaClave1", "NuevaClave1"))
        .isInstanceOf(UsuarioNoEncontradoException.class)
        .hasMessageContaining(EMAIL);
  }

  @Test
  void cambiarPassword_nuevaMuyCorta_lanzaPasswordInvalida() {
    assertThatThrownBy(() -> cambiar("Actual123", "corta", "corta"))
        .isInstanceOf(PasswordInvalidaException.class)
        .hasMessage("La nueva contraseña debe tener al menos 8 caracteres");
  }

  @Test
  void cambiarPassword_nuevaNula_lanzaPasswordInvalida() {
    assertThatThrownBy(() -> cambiar("Actual123", null, null))
        .isInstanceOf(PasswordInvalidaException.class)
        .hasMessage("La nueva contraseña debe tener al menos 8 caracteres");
  }

  @Test
  void cambiarPassword_actualIncorrecta_lanzaPasswordInvalida() {
    assertThatThrownBy(() -> cambiar("Equivocada1", "NuevaClave1", "NuevaClave1"))
        .isInstanceOf(PasswordInvalidaException.class)
        .hasMessage("La contraseña actual es incorrecta");
  }

  @Test
  void cambiarPassword_confirmacionDistinta_lanzaPasswordInvalida() {
    assertThatThrownBy(() -> cambiar("Actual123", "NuevaClave1", "OtraClave1"))
        .isInstanceOf(PasswordInvalidaException.class)
        .hasMessage("Las nuevas contraseñas no coinciden");
  }

  @Test
  void cambiarPassword_igualALaAnterior_lanzaPasswordInvalida() {
    assertThatThrownBy(() -> cambiar("Actual123", "Actual123", "Actual123"))
        .isInstanceOf(PasswordInvalidaException.class)
        .hasMessage("La nueva contraseña no puede ser igual a la anterior");
    verify(usuarioRepository, never()).save(any());
  }

  @Test
  void cambiarPassword_valida_guardaLaNuevaEncriptada() {
    when(passwordEncoder.encode("NuevaClave1")).thenReturn("hash-nuevo");

    cambiar("Actual123", "NuevaClave1", "NuevaClave1");

    assertThat(laura.getPassword()).isEqualTo("hash-nuevo");
    verify(usuarioRepository).save(laura);
  }
}
