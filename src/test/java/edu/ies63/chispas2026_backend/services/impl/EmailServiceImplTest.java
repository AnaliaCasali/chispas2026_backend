package edu.ies63.chispas2026_backend.services.impl;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// tests unitarios del correo de recuperacion con el JavaMailSender mockeado (no manda mails de verdad)
class EmailServiceImplTest {

  private final JavaMailSender mailSender = mock(JavaMailSender.class);

  private EmailServiceImpl servicioCon(String frontendUrl) {
    EmailServiceImpl emailService = new EmailServiceImpl(mailSender);
    ReflectionTestUtils.setField(emailService, "fromEmail", "chispas@gmail.com");
    ReflectionTestUtils.setField(emailService, "frontendUrl", frontendUrl);
    return emailService;
  }

  private SimpleMailMessage mensajeEnviado() {
    ArgumentCaptor<SimpleMailMessage> mensaje = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mailSender).send(mensaje.capture());
    return mensaje.getValue();
  }

  @Test
  void mandaElCorreoConElLinkDeRecuperacion() {
    servicioCon("http://localhost:4200").sendPasswordResetEmail("docente@chispas.edu.ar", "abc-123");

    SimpleMailMessage mensaje = mensajeEnviado();
    assertThat(mensaje.getFrom()).isEqualTo("chispas@gmail.com");
    assertThat(mensaje.getTo()).containsExactly("docente@chispas.edu.ar");
    assertThat(mensaje.getSubject()).isEqualTo("Recuperación de Contraseña");
    assertThat(mensaje.getText())
        .contains("http://localhost:4200/reset-password?token=abc-123")
        .contains("15 minutos");
  }

  // si la url del front termina en / no queda la barra doble
  @Test
  void urlDelFrontConBarraFinal_noDuplicaLaBarra() {
    servicioCon("https://chispas.edu.ar/").sendPasswordResetEmail("docente@chispas.edu.ar", "abc-123");

    assertThat(mensajeEnviado().getText()).contains("https://chispas.edu.ar/reset-password?token=abc-123");
  }

  // si falla el envio solo se loguea, no rompe la recuperacion
  @Test
  void errorAlEnviar_noLanzaExcepcion() {
    doThrow(new MailSendException("smtp caido")).when(mailSender).send(any(SimpleMailMessage.class));

    assertThatCode(() -> servicioCon("http://localhost:4200").sendPasswordResetEmail("docente@chispas.edu.ar", "x"))
        .doesNotThrowAnyException();
  }
}
