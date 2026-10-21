package edu.ies63.chispas2026_backend.security.jwt;

import edu.ies63.chispas2026_backend.models.Usuario;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// tests unitarios de JwtService con una clave de prueba (no usa la del .env)
class JwtServiceTest {

  private static final String CLAVE = Base64.getEncoder()
      .encodeToString("clave-de-prueba-de-32-caracteres!".getBytes());
  private static final String OTRA_CLAVE = Base64.getEncoder()
      .encodeToString("otra-clave-de-prueba-de-32-chars".getBytes());

  private final Usuario laura = Usuario.builder().email("docente@chispas.edu.ar").dni("20111222").build();
  private final Usuario mariel = Usuario.builder().dni("45123456").build();

  private JwtService servicio(String clave, long expiracionMs) {
    JwtService jwtService = new JwtService();
    ReflectionTestUtils.setField(jwtService, "secretKey", clave);
    ReflectionTestUtils.setField(jwtService, "jwtExpiration", expiracionMs);
    return jwtService;
  }

  private final JwtService jwtService = servicio(CLAVE, 60_000);

  // el subject es el username (email, o DNI si no tiene email) y el rol viaja como claim
  @Test
  void generaTokenConUsernameYRol() {
    String token = jwtService.generateToken(Map.of("rol", "DOCENTE"), laura);

    assertThat(jwtService.extractUsername(token)).isEqualTo("docente@chispas.edu.ar");
    assertThat(jwtService.extractRol(token)).isEqualTo("DOCENTE");
  }

  @Test
  void usuarioSinEmail_usaElDniComoSubject() {
    String token = jwtService.generateToken(Map.of(), mariel);

    assertThat(jwtService.extractUsername(token)).isEqualTo("45123456");
    assertThat(jwtService.extractRol(token)).isNull();
  }

  @Test
  void tokenEsValidoParaSuUsuario() {
    String token = jwtService.generateToken(Map.of(), laura);

    assertThat(jwtService.isTokenValid(token, laura)).isTrue();
  }

  @Test
  void tokenNoEsValidoParaOtroUsuario() {
    String token = jwtService.generateToken(Map.of(), laura);

    assertThat(jwtService.isTokenValid(token, mariel)).isFalse();
  }

  // token vencido: jjwt lo rechaza al leerlo
  @Test
  void tokenVencido_lanzaExpiredJwt() {
    String token = servicio(CLAVE, -1_000).generateToken(Map.of(), laura);

    assertThatThrownBy(() -> jwtService.isTokenValid(token, laura)).isInstanceOf(ExpiredJwtException.class);
  }

  // token firmado con otra clave: se rechaza la firma
  @Test
  void tokenConOtraFirma_lanzaJwtException() {
    String token = servicio(OTRA_CLAVE, 60_000).generateToken(Map.of(), laura);

    assertThatThrownBy(() -> jwtService.extractUsername(token)).isInstanceOf(JwtException.class);
  }

  @Test
  void tokenMalFormado_lanzaJwtException() {
    assertThatThrownBy(() -> jwtService.extractUsername("esto.no.es-un-jwt")).isInstanceOf(JwtException.class);
  }
}
