package edu.ies63.chispas2026_backend.controllers;

import edu.ies63.chispas2026_backend.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Import;

import java.time.Clock;
import java.time.LocalDate;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// TSDS-6: test de integracion del Inicio (app completa + MySQL con los datos de ejemplo).
// Mariel tiene Lengua = carpetaEspacio 3 (tarea 1 FINALIZADA y tarea 2 PENDIENTE), Historia = 5 sin tareas.
// Las materias se buscan por carpetaEspacioId porque el orden depende del dia y la hora en que corra el test.
// Cada test corre en una transaccion que se deshace al final.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestcontainersConfiguration.class)
class InicioControllerTest {

  private static final String URL_INICIO = "/api/v1/inicio";
  private static final String LENGUA = "$.materias[?(@.carpetaEspacioId == 3)]";
  private static final String HISTORIA = "$.materias[?(@.carpetaEspacioId == 5)]";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JdbcTemplate jdbc;

  @Autowired
  private Clock clock;

  // hace login con el endpoint real y devuelve el header Authorization
  private String bearer(String usuario) throws Exception {
    String body = mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"%s\",\"password\":\"Chispas2026\"}".formatted(usuario)))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    return "Bearer " + JsonPath.read(body, "$.token");
  }

  // la asignada hoy en Lengua es la principal, la 2 queda en otrasPendientes y la 1 (FINALIZADA) no aparece
  @Test
  void devuelveCadaMateriaConSuPrincipalYSusPendientes() throws Exception {
    LocalDate hoy = LocalDate.now(clock);
    jdbc.update("""
        INSERT INTO tarea (actividad_id, carpeta_espacio_id, asignada_por, fecha_asignacion, asignada_en, estado)
        VALUES (2, 3, 1, ?, ?, 'PENDIENTE')""", hoy, hoy + " 08:00:00");
    long deHoy = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

    mockMvc.perform(get(URL_INICIO).header("Authorization", bearer("45123456")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.materias", hasSize(3)))
        .andExpect(jsonPath(LENGUA + ".nombre").value("Lengua y Literatura"))
        .andExpect(jsonPath(LENGUA + ".actividadPrincipal.tareaId").value((int) deHoy))
        .andExpect(jsonPath(LENGUA + ".actividadPrincipal.esHoy").value(true))
        .andExpect(jsonPath(LENGUA + ".actividadPrincipal.estado").value("Pendiente"))
        .andExpect(jsonPath(LENGUA + ".otrasPendientes[*].tareaId", contains(2)))
        .andExpect(jsonPath(LENGUA + ".mensaje", contains(nullValue())));
  }

  // materia sin pendientes: principal null, lista vacia y mensaje
  @Test
  void materiaSinPendientes_devuelvePrincipalNullYMensaje() throws Exception {
    mockMvc.perform(get(URL_INICIO).header("Authorization", bearer("45123456")))
        .andExpect(status().isOk())
        .andExpect(jsonPath(HISTORIA + ".actividadPrincipal", contains(nullValue())))
        .andExpect(jsonPath(HISTORIA + ".otrasPendientes[*]", hasSize(0)))
        .andExpect(jsonPath(HISTORIA + ".mensaje").value("No tienes actividades pendientes en esta materia"));
  }

  // solo ve lo de su carpeta: Tomas no ve la tarea pendiente de Lengua de Mariel, sino la suya (tarea 3)
  @Test
  void cadaAlumnoVeSoloSusPendientes() throws Exception {
    mockMvc.perform(get(URL_INICIO).header("Authorization", bearer("45654321")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.materias", hasSize(2)))
        .andExpect(jsonPath("$.materias[?(@.carpetaEspacioId == 6)].actividadPrincipal.tareaId").value(3));
  }

  // sin token: 401 con el mensaje del entry point de SecurityConfig
  @Test
  void sinToken_devuelve401() throws Exception {
    mockMvc.perform(get(URL_INICIO))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Debes iniciar sesión"));
  }

  // un docente no es estudiante: 403 por rol
  @Test
  void usuarioQueNoEsEstudiante_devuelve403() throws Exception {
    mockMvc.perform(get(URL_INICIO).header("Authorization", bearer("docente@chispas.edu.ar")))
        .andExpect(status().isForbidden());
  }
}
