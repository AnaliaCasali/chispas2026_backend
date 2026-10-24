package edu.ies63.chispas2026_backend.security.auth;

import edu.ies63.chispas2026_backend.services.EmailService;
import edu.ies63.chispas2026_backend.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// test de integracion de /api/v1/auth (app completa + MySQL con los datos de ejemplo).
// Usa a Laura (docente@chispas.edu.ar, id 1, clave Chispas2026). El EmailService esta mockeado para no mandar mails.
// Cada test corre en una transaccion que se deshace al final.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestcontainersConfiguration.class)
class AuthControllerTest {

  private static final String LAURA = "docente@chispas.edu.ar";
  private static final String CLAVE = "Chispas2026";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JdbcTemplate jdbc;

  @MockitoBean
  private EmailService emailService;

  private ResultActions postJson(String url, String json) throws Exception {
    return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
  }

  private ResultActions login(String usuario, String clave) throws Exception {
    return postJson("/api/v1/auth/login", "{\"username\":\"%s\",\"password\":\"%s\"}".formatted(usuario, clave));
  }

  private void guardarTokenDeReset(String token, String vence) {
    jdbc.update("INSERT INTO password_reset_tokens (token, usuario_id, expiry_date) VALUES (?, 1, " + vence + ")",
        token);
  }

  private ResultActions resetPassword(String token, String nueva, String confirmacion) throws Exception {
    return postJson("/api/v1/auth/reset-password",
        "{\"token\":\"%s\",\"newPassword\":\"%s\",\"confirmationPassword\":\"%s\"}"
            .formatted(token, nueva, confirmacion));
  }

  // ---------- login ----------

  @Test
  void login_conEmail_devuelveToken() throws Exception {
    login(LAURA, CLAVE)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value(not(emptyString())));
  }

  @Test
  void login_passwordIncorrecta_devuelve401() throws Exception {
    login(LAURA, "otraClave99")
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Credenciales incorrectas"));
  }

  @Test
  void login_usuarioInexistente_devuelve401() throws Exception {
    login("nadie@chispas.edu.ar", CLAVE)
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
  }

  // body vacio: 400 con el error de cada campo
  @Test
  void login_sinDatos_devuelve400ConLosCampos() throws Exception {
    postJson("/api/v1/auth/login", "{}")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Error de validación"))
        .andExpect(jsonPath("$.details.username").value("El email o DNI es obligatorio"))
        .andExpect(jsonPath("$.details.password").value("La contraseña es obligatoria"));
  }

  // ---------- registro ----------

  // el usuario registrado queda como ESTUDIANTE y despues puede iniciar sesion
  @Test
  void registro_creaEstudianteQuePuedeIniciarSesion() throws Exception {
    postJson("/api/v1/auth/register/usuario", """
        {"email":"ana@chispas.edu.ar","password":"ClaveAna1","nombre":"Ana","apellido":"Pérez","dni":"40111222"}""")
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token").value(not(emptyString())));

    String rol = jdbc.queryForObject("SELECT rol FROM usuario WHERE email = 'ana@chispas.edu.ar'", String.class);
    assertThat(rol).isEqualTo("ESTUDIANTE");
    login("ana@chispas.edu.ar", "ClaveAna1").andExpect(status().isOk());
  }

  @Test
  void registro_emailRepetido_devuelve409() throws Exception {
    postJson("/api/v1/auth/register/usuario", """
        {"email":"docente@chispas.edu.ar","password":"ClaveAna1","nombre":"Ana","apellido":"Pérez","dni":"40111222"}""")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("Conflicto de registro"))
        .andExpect(jsonPath("$.details").value("El email ya está registrado: docente@chispas.edu.ar"));
  }

  @Test
  void registro_datosInvalidos_devuelve400() throws Exception {
    postJson("/api/v1/auth/register/usuario", """
        {"email":"no-es-un-email","password":"corta","nombre":"A","apellido":"","dni":"123"}""")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.details.email").value("Debe ingresar un correo electrónico válido"))
        .andExpect(jsonPath("$.details.password").exists())
        .andExpect(jsonPath("$.details.nombre").exists())
        .andExpect(jsonPath("$.details.apellido").exists())
        .andExpect(jsonPath("$.details.dni").exists());
  }

  // ---------- olvide mi contraseña ----------

  // guarda un token para Laura y le manda el correo con ese token
  @Test
  void olvidePassword_guardaElTokenYMandaElCorreo() throws Exception {
    postJson("/api/v1/auth/forgot-password", "{\"email\":\"docente@chispas.edu.ar\"}")
        .andExpect(status().isOk());

    String token = jdbc.queryForObject("SELECT token FROM password_reset_tokens WHERE usuario_id = 1", String.class);
    verify(emailService).sendPasswordResetEmail(eq(LAURA), eq(token));
  }

  // email desconocido: igual responde 200 para no revelar que emails existen
  @Test
  void olvidePassword_emailDesconocido_responde200SinMandarCorreo() throws Exception {
    postJson("/api/v1/auth/forgot-password", "{\"email\":\"nadie@chispas.edu.ar\"}")
        .andExpect(status().isOk());

    verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
  }

  // ---------- reset de contraseña ----------

  // con un token vigente cambia la clave: la nueva sirve y la vieja no
  @Test
  void resetPassword_tokenVigente_cambiaLaClave() throws Exception {
    guardarTokenDeReset("token-vigente", "NOW() + INTERVAL 10 MINUTE");

    resetPassword("token-vigente", "NuevaClave1", "NuevaClave1").andExpect(status().isOk());

    login(LAURA, "NuevaClave1").andExpect(status().isOk());
    login(LAURA, CLAVE).andExpect(status().isUnauthorized());
    Integer tokens = jdbc.queryForObject("SELECT COUNT(*) FROM password_reset_tokens WHERE usuario_id = 1", Integer.class);
    assertThat(tokens).isZero();
  }

  @Test
  void resetPassword_tokenVencido_devuelve400() throws Exception {
    guardarTokenDeReset("token-vencido", "NOW() - INTERVAL 1 MINUTE");

    resetPassword("token-vencido", "NuevaClave1", "NuevaClave1")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("El token de recuperación ha expirado"));
    login(LAURA, CLAVE).andExpect(status().isOk());
  }

  @Test
  void resetPassword_tokenInexistente_devuelve400() throws Exception {
    resetPassword("no-existe", "NuevaClave1", "NuevaClave1")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Token de recuperación inválido o inexistente"));
  }

  @Test
  void resetPassword_confirmacionDistinta_devuelve400() throws Exception {
    resetPassword("cualquiera", "NuevaClave1", "OtraClave1")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Las contraseñas no coinciden"));
  }
}
