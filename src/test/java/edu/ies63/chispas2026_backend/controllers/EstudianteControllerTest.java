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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// test de integracion del perfil del estudiante (app completa + MySQL con los datos de ejemplo).
// Laura (docente) tiene a Mariel (TDA y Dislexia) y a Tomás (sin condiciones) en 4to Año A.
// Lucía (Autismo) es de Pablo en 5to Año B: Laura no la tiene asignada.
// Cada test corre en una transaccion que se deshace al final.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestcontainersConfiguration.class)
class EstudianteControllerTest {

  private static final String URL_PERFIL = "/api/v1/estudiantes/{id}/perfil";
  private static final String URL_MI_PERFIL = "/api/v1/estudiantes/mi-perfil";
  private static final String URL_LISTA = "/api/v1/estudiantes";
  private static final String LAURA = "docente@chispas.edu.ar";
  private static final String MARIEL = "45123456";
  private static final String TOMAS = "45654321";
  private static final String SIN_PERMISO = "No tienes permiso para ver este perfil";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JdbcTemplate jdbc;

  // hace login con el endpoint real y devuelve el header Authorization
  private String bearer(String usuario) throws Exception {
    String body = mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"%s\",\"password\":\"Chispas2026\"}".formatted(usuario)))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    return "Bearer " + JsonPath.read(body, "$.token");
  }

  // ---------- docente ----------

  // escenario 1 y 4: perfil de un alumno asignado con todas sus condiciones
  @Test
  void docente_veElPerfilDeSuAlumnoConTodasSusCondiciones() throws Exception {
    mockMvc.perform(get(URL_PERFIL, 2).header("Authorization", bearer(LAURA)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(2))
        .andExpect(jsonPath("$.nombre").value("Mariel"))
        .andExpect(jsonPath("$.apellido").value("Linera"))
        .andExpect(jsonPath("$.fotoUrl").value(nullValue()))
        .andExpect(jsonPath("$.curso").value("4to Año A"))
        .andExpect(jsonPath("$.condiciones", contains("Dislexia", "TDA")))
        .andExpect(jsonPath("$.mensaje").value(nullValue()));
  }

  // escenario 3: sin condiciones va [] y el mensaje
  @Test
  void docente_alumnoSinCondiciones_listaVaciaYMensaje() throws Exception {
    mockMvc.perform(get(URL_PERFIL, 3).header("Authorization", bearer(LAURA)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nombre").value("Tomás"))
        .andExpect(jsonPath("$.condiciones", hasSize(0)))
        .andExpect(jsonPath("$.mensaje").value("Sin condición registrada"));
  }

  // escenario 5: Lucía no es alumna de Laura
  @Test
  void docente_alumnoNoAsignado_devuelve403() throws Exception {
    mockMvc.perform(get(URL_PERFIL, 5).header("Authorization", bearer(LAURA)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value(SIN_PERMISO));
  }

  @Test
  void docente_estudianteInexistente_devuelve404() throws Exception {
    mockMvc.perform(get(URL_PERFIL, 999).header("Authorization", bearer(LAURA)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("El estudiante no existe"));
  }

  // el id de un docente no es un estudiante
  @Test
  void docente_idDeUnDocente_devuelve404() throws Exception {
    mockMvc.perform(get(URL_PERFIL, 4).header("Authorization", bearer(LAURA)))
        .andExpect(status().isNotFound());
  }

  @Test
  void docente_listaSoloSusAlumnosOrdenadosPorApellido() throws Exception {
    mockMvc.perform(get(URL_LISTA).header("Authorization", bearer(LAURA)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id").value(2))
        .andExpect(jsonPath("$[0].apellido").value("Linera"))
        .andExpect(jsonPath("$[0].curso").value("4to Año A"))
        .andExpect(jsonPath("$[1].id").value(3))
        .andExpect(jsonPath("$[1].nombre").value("Tomás"));
  }

  @Test
  void docente_noTieneMiPerfil_devuelve403() throws Exception {
    mockMvc.perform(get(URL_MI_PERFIL).header("Authorization", bearer(LAURA)))
        .andExpect(status().isForbidden());
  }

  // ---------- alumno ----------

  // escenario 2: el alumno ve su perfil desde "Mi perfil"
  @Test
  void alumno_veSuPerfil() throws Exception {
    mockMvc.perform(get(URL_MI_PERFIL).header("Authorization", bearer(MARIEL)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(2))
        .andExpect(jsonPath("$.nombre").value("Mariel"))
        .andExpect(jsonPath("$.curso").value("4to Año A"))
        .andExpect(jsonPath("$.condiciones", contains("Dislexia", "TDA")));
  }

  @Test
  void alumno_veSuPerfilTambienPorId() throws Exception {
    mockMvc.perform(get(URL_PERFIL, 3).header("Authorization", bearer(TOMAS)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.mensaje").value("Sin condición registrada"));
  }

  // escenario 5: un compañero de curso tampoco puede ver el perfil de otro
  @Test
  void alumno_perfilDeOtroAlumno_devuelve403() throws Exception {
    mockMvc.perform(get(URL_PERFIL, 2).header("Authorization", bearer(TOMAS)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value(SIN_PERMISO));
  }

  // con un id que no existe tampoco se revela nada: 403, no 404
  @Test
  void alumno_idInexistente_devuelve403() throws Exception {
    mockMvc.perform(get(URL_PERFIL, 999).header("Authorization", bearer(MARIEL)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value(SIN_PERMISO));
  }

  @Test
  void alumno_noPuedeVerLaListaDeEstudiantes() throws Exception {
    mockMvc.perform(get(URL_LISTA).header("Authorization", bearer(MARIEL)))
        .andExpect(status().isForbidden());
  }

  // ---------- seguridad ----------

  // ninguno es publico: sin token 401
  @Test
  void sinToken_losTresDevuelven401() throws Exception {
    mockMvc.perform(get(URL_PERFIL, 2)).andExpect(status().isUnauthorized());
    mockMvc.perform(get(URL_MI_PERFIL)).andExpect(status().isUnauthorized());
    mockMvc.perform(get(URL_LISTA)).andExpect(status().isUnauthorized());
  }

  // un rol que no es docente ni alumno: 403 por rol
  @Test
  void preceptor_noPuedeVerPerfiles() throws Exception {
    jdbc.update("UPDATE usuario SET rol = 'PRECEPTOR' WHERE id = 4");

    mockMvc.perform(get(URL_PERFIL, 5).header("Authorization", bearer("docente2@chispas.edu.ar")))
        .andExpect(status().isForbidden());
  }
}
