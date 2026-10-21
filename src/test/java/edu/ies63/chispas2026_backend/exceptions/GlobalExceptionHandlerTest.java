package edu.ies63.chispas2026_backend.exceptions;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// tests unitarios del formato de las respuestas de error (los de validacion @Valid se prueban en AuthControllerTest)
class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  private void esError(ResponseEntity<Map<String, Object>> respuesta, HttpStatus status, String mensaje) {
    assertThat(respuesta.getStatusCode()).isEqualTo(status);
    assertThat(respuesta.getBody())
        .containsEntry("status", status.value())
        .containsEntry("error", status.getReasonPhrase())
        .containsEntry("message", mensaje)
        .containsKey("timestamp");
  }

  @Test
  void entidadNoEncontrada_404ConElMensaje() {
    var respuesta = handler.handleEntityNotFound(new EntityNotFoundException("La materia solicitada no existe"));

    esError(respuesta, HttpStatus.NOT_FOUND, "La materia solicitada no existe");
    assertThat(respuesta.getBody()).doesNotContainKey("details");
  }

  @Test
  void responseStatusException_usaSuStatusYSuMotivo() {
    var respuesta = handler.handleResponseStatusException(
        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

    esError(respuesta, HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
  }

  @Test
  void usuarioNoEncontradoPorUsername_404() {
    var respuesta = handler.handleUsuarioNoEncontrado(new UsuarioNoEncontradoException("ana@chispas.edu.ar"));

    esError(respuesta, HttpStatus.NOT_FOUND, "Usuario no encontrado: ana@chispas.edu.ar");
  }

  @Test
  void usuarioNoEncontradoPorId_404() {
    var respuesta = handler.handleUsuarioNoEncontrado(new UsuarioNoEncontradoException(7L));

    esError(respuesta, HttpStatus.NOT_FOUND, "Usuario con id 7 no encontrado");
  }

  @Test
  void emailRepetido_409ConDetalle() {
    var respuesta = handler.handleEmailAlreadyRegistered(
        new EmailYaRegistradoException("El email ya está registrado: ana@chispas.edu.ar"));

    esError(respuesta, HttpStatus.CONFLICT, "Conflicto de registro");
    assertThat(respuesta.getBody()).containsEntry("details", "El email ya está registrado: ana@chispas.edu.ar");
  }

  @Test
  void passwordInvalida_400ConDetalle() {
    var respuesta = handler.handleInvalidPassword(new PasswordInvalidaException("La contraseña actual es incorrecta"));

    esError(respuesta, HttpStatus.BAD_REQUEST, "Error de validación de seguridad");
    assertThat(respuesta.getBody()).containsEntry("details", "La contraseña actual es incorrecta");
  }

  @Test
  void accesoDenegadoACarpeta_403ConElMensaje() {
    var respuesta = handler.handleAccesoDenegado(new AccesoDenegadoException("No tienes permiso para ver esta carpeta"));

    esError(respuesta, HttpStatus.FORBIDDEN, "No tienes permiso para ver esta carpeta");
  }

  @Test
  void rolSinPermiso_403ConMensajeGenerico() {
    var respuesta = handler.handleAccessDenied(new AccessDeniedException("Access Denied"));

    esError(respuesta, HttpStatus.FORBIDDEN, "No tienes permiso para acceder a este recurso");
  }

  @Test
  void credencialesIncorrectas_401() {
    var respuesta = handler.handleBadCredentials(new BadCredentialsException("Bad credentials"));

    esError(respuesta, HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
  }

  @Test
  void errorInesperado_500ConElDetalle() {
    var respuesta = handler.handleGenericException(new IllegalStateException("se rompio algo"));

    esError(respuesta, HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    assertThat(respuesta.getBody()).containsEntry("details", "se rompio algo");
  }
}
