package edu.ies63.chispas2026_backend.security.jwt;

import edu.ies63.chispas2026_backend.enums.Role;
import edu.ies63.chispas2026_backend.models.Usuario;
import edu.ies63.chispas2026_backend.services.impl.UsuarioServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// tests unitarios del filtro JWT con request y response de prueba
class JwtAuthFilterTest {

  private final JwtService jwtService = mock(JwtService.class);
  private final UsuarioServiceImpl usuarioService = mock(UsuarioServiceImpl.class);
  private final FilterChain chain = mock(FilterChain.class);
  private final JwtAuthFilter filtro = new JwtAuthFilter(jwtService, usuarioService);

  private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/inicio");
  private final MockHttpServletResponse response = new MockHttpServletResponse();
  private final Usuario mariel = Usuario.builder().dni("45123456").role(Role.ESTUDIANTE).activo(true).build();

  @AfterEach
  void limpiarContexto() {
    SecurityContextHolder.clearContext();
  }

  private void conToken(String token) {
    request.addHeader("Authorization", "Bearer " + token);
  }

  private Authentication autenticacion() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  // ---------- whitelist ----------

  @Test
  void noFiltraLasRutasPublicas() {
    for (String ruta : List.of("/api/v1/auth/login", "/api/v1/auth/register/usuario", "/swagger-ui/index.html",
        "/v3/api-docs/swagger-config", "/actuator/health", "/api/v1/auth/forgot-password",
        "/api/v1/auth/reset-password")) {
      assertThat(filtro.shouldNotFilter(new MockHttpServletRequest("POST", ruta))).as(ruta).isTrue();
    }
  }

  @Test
  void filtraLasRutasProtegidas() {
    assertThat(filtro.shouldNotFilter(request)).isFalse();
  }

  // ---------- sin token ----------

  @Test
  void sinHeader_sigueLaCadenaSinAutenticar() throws Exception {
    filtro.doFilterInternal(request, response, chain);

    verify(chain).doFilter(request, response);
    verifyNoInteractions(jwtService);
    assertThat(autenticacion()).isNull();
  }

  @Test
  void headerQueNoEsBearer_sigueLaCadenaSinAutenticar() throws Exception {
    request.addHeader("Authorization", "Basic abc123");

    filtro.doFilterInternal(request, response, chain);

    verify(chain).doFilter(request, response);
    verifyNoInteractions(jwtService);
  }

  // ---------- con token ----------

  // token valido: autentica al usuario con sus roles y sigue la cadena
  @Test
  void tokenValido_autenticaAlUsuario() throws Exception {
    conToken("jwt");
    when(jwtService.extractUsername("jwt")).thenReturn("45123456");
    when(usuarioService.loadUserByUsername("45123456")).thenReturn(mariel);
    when(jwtService.isTokenValid("jwt", mariel)).thenReturn(true);

    filtro.doFilterInternal(request, response, chain);

    verify(chain).doFilter(request, response);
    assertThat(autenticacion().getPrincipal()).isSameAs(mariel);
    assertThat(autenticacion().getAuthorities()).extracting("authority")
        .containsExactlyInAnyOrder("ROLE_ESTUDIANTE", "ESTUDIANTE");
  }

  // token que no corresponde al usuario: sigue la cadena pero sin autenticar (despues Security da 401)
  @Test
  void tokenNoValido_noAutentica() throws Exception {
    conToken("jwt");
    when(jwtService.extractUsername("jwt")).thenReturn("45123456");
    when(usuarioService.loadUserByUsername("45123456")).thenReturn(mariel);
    when(jwtService.isTokenValid("jwt", mariel)).thenReturn(false);

    filtro.doFilterInternal(request, response, chain);

    verify(chain).doFilter(request, response);
    assertThat(autenticacion()).isNull();
  }

  // si ya hay alguien autenticado no vuelve a buscar el usuario
  @Test
  void yaAutenticado_noBuscaDeNuevoElUsuario() throws Exception {
    conToken("jwt");
    Authentication previa = new UsernamePasswordAuthenticationToken("otro", null, List.of());
    SecurityContextHolder.getContext().setAuthentication(previa);
    when(jwtService.extractUsername("jwt")).thenReturn("45123456");

    filtro.doFilterInternal(request, response, chain);

    verifyNoInteractions(usuarioService);
    verify(chain).doFilter(request, response);
    assertThat(autenticacion()).isSameAs(previa);
  }

  // token sin subject: no busca usuario
  @Test
  void tokenSinUsername_noBuscaUsuario() throws Exception {
    conToken("jwt");
    when(jwtService.extractUsername("jwt")).thenReturn(null);

    filtro.doFilterInternal(request, response, chain);

    verifyNoInteractions(usuarioService);
    verify(chain).doFilter(request, response);
  }

  // ---------- errores: responde 401 y corta la cadena ----------

  @Test
  void tokenVencido_responde401TokenExpirado() throws Exception {
    conToken("jwt");
    when(jwtService.extractUsername("jwt")).thenThrow(new ExpiredJwtException(null, null, "vencido"));

    filtro.doFilterInternal(request, response, chain);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getContentType()).startsWith("application/json");
    assertThat(response.getContentAsString()).contains("\"message\":\"Token expirado\"");
    verify(chain, never()).doFilter(any(), any());
  }

  @Test
  void tokenInvalido_responde401TokenInvalido() throws Exception {
    conToken("jwt");
    when(jwtService.extractUsername("jwt")).thenThrow(new MalformedJwtException("mal formado"));

    filtro.doFilterInternal(request, response, chain);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getContentAsString()).contains("\"message\":\"Token inválido\"");
    verify(chain, never()).doFilter(any(), any());
  }

  // usuario del token que ya no existe: error de autenticacion
  @Test
  void errorInesperado_responde401ErrorDeAutenticacion() throws Exception {
    conToken("jwt");
    when(jwtService.extractUsername("jwt")).thenReturn("borrado@chispas.edu.ar");
    when(usuarioService.loadUserByUsername("borrado@chispas.edu.ar"))
        .thenThrow(new UsernameNotFoundException("no existe"));

    filtro.doFilterInternal(request, response, chain);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getContentAsString()).contains("\"message\":\"Error de autenticación\"");
    verify(chain, never()).doFilter(any(), any());
  }
}
