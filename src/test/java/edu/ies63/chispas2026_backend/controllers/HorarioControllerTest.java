package edu.ies63.chispas2026_backend.controllers;

import edu.ies63.chispas2026_backend.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import edu.ies63.chispas2026_backend.enums.DiaSemana;
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
import java.util.Optional;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// test de integracion del horario (app completa + MySQL con chispas_bd.sql).
// Mariel (carpeta 2) cursa Lengua, Matematica e Historia; Tomas (carpeta 3) solo Lengua y Matematica,
// los dos en 4to A (curso 1). Como depende del dia real, los tests de dia habil cargan su propio
// horario para hoy y se saltean el fin de semana. Cada test se deshace al final.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestcontainersConfiguration.class)
class HorarioControllerTest {

  private static final String URL_HORARIO = "/api/v1/mi-horario";
  private static final String MARIEL = "45123456";
  private static final String TOMAS = "45654321";

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

  private Optional<DiaSemana> hoy() {
    return DiaSemana.desde(LocalDate.now(clock).getDayOfWeek());
  }

  // reemplaza el horario de 4to A por uno conocido para hoy, cargado desordenado
  private DiaSemana cargarHorarioDeHoy() {
    DiaSemana dia = hoy().orElseThrow();
    jdbc.update("DELETE FROM horario WHERE curso_id = 1");
    jdbc.update("""
        INSERT INTO horario (curso_id, espacio_curricular_id, docente_id, dia, hora_inicio, hora_fin) VALUES
          (1, 3, NULL, ?, '10:00', '11:00'),
          (1, 1, 1,    ?, '07:00', '08:00'),
          (1, 2, NULL, ?, '08:00', '09:00')""", dia.name(), dia.name(), dia.name());
    return dia;
  }

  @Test
  void devuelveLasClasesDeHoyOrdenadasPorHora() throws Exception {
    assumeTrue(hoy().isPresent(), "el fin de semana no hay clases");
    DiaSemana dia = cargarHorarioDeHoy();

    mockMvc.perform(get(URL_HORARIO).header("Authorization", bearer(MARIEL)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.dia").value(dia.name()))
        .andExpect(jsonPath("$.mensaje").doesNotExist())
        .andExpect(jsonPath("$.clases[*].materia", contains("Lengua y Literatura", "Matemática", "Historia")))
        .andExpect(jsonPath("$.clases[0].carpetaEspacioId").value(3))
        .andExpect(jsonPath("$.clases[0].color").value("#4F7BE8"))
        .andExpect(jsonPath("$.clases[0].horaInicio").value("07:00:00"))
        .andExpect(jsonPath("$.clases[0].horaFin").value("08:00:00"))
        .andExpect(jsonPath("$.clases[0].ahora").isBoolean());
  }

  // Historia es del curso pero no esta en la carpeta de Tomas
  @Test
  void soloMuestraLasMateriasDeLaCarpeta() throws Exception {
    assumeTrue(hoy().isPresent(), "el fin de semana no hay clases");
    cargarHorarioDeHoy();

    mockMvc.perform(get(URL_HORARIO).header("Authorization", bearer(TOMAS)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.clases[*].materia", contains("Lengua y Literatura", "Matemática")));
  }

  @Test
  void sinClasesHoy_devuelveMensajeYListaVacia() throws Exception {
    jdbc.update("DELETE FROM horario WHERE curso_id = 1");

    mockMvc.perform(get(URL_HORARIO).header("Authorization", bearer(MARIEL)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.mensaje").value("Hoy no tienes clases"))
        .andExpect(jsonPath("$.clases", hasSize(0)));
  }

  @Test
  void sinToken_devuelve401() throws Exception {
    mockMvc.perform(get(URL_HORARIO))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void usuarioQueNoEsEstudiante_devuelve403() throws Exception {
    mockMvc.perform(get(URL_HORARIO).header("Authorization", bearer("docente@chispas.edu.ar")))
        .andExpect(status().isForbidden());
  }
}
