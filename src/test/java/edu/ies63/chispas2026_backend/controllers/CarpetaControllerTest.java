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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// TSDS-4: test de integracion del endpoint de la carpeta (app completa + MySQL con chispas_bd.sql).
// Usa los datos de ejemplo: Mariel (DNI 45123456) con Lengua = carpetaEspacio 3, Historia = 5 (sin tareas),
// Tomas tiene carpetaEspacio 6. Cada test corre en una transaccion que se deshace al final.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestcontainersConfiguration.class)
class CarpetaControllerTest {

  private static final String URL_ACTIVIDADES = "/api/v1/mi-carpeta/materias/{id}/actividades";
  private static final String MARIEL = "45123456";
  private static final String DOCENTE = "docente@chispas.edu.ar";

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

  // inserta una tarea para Lengua de Mariel y devuelve su id
  private long insertarTareaEnLengua(long actividadId, LocalDate fecha, String hora) {
    jdbc.update("""
        INSERT INTO tarea (actividad_id, carpeta_espacio_id, asignada_por, fecha_asignacion, asignada_en, estado)
        VALUES (?, 3, 1, ?, ?, 'PENDIENTE')""", actividadId, fecha, fecha + " " + hora);
    return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
  }

  // la de hoy va primera con esHoy, la de mañana no aparece y las del mismo dia salen por hora
  @Test
  void devuelveLasTareasOrdenadasConTodosLosCampos() throws Exception {
    LocalDate hoy = LocalDate.now(clock);
    long deHoy = insertarTareaEnLengua(2, hoy, "10:00:00");
    insertarTareaEnLengua(4, hoy.plusDays(1), "10:00:00");

    mockMvc.perform(get(URL_ACTIVIDADES, 3).header("Authorization", bearer(MARIEL)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.materia").value("Lengua y Literatura"))
        .andExpect(jsonPath("$.mensaje").doesNotExist())
        .andExpect(jsonPath("$.actividades", hasSize(3)))
        // tarea de hoy
        .andExpect(jsonPath("$.actividades[0].tareaId").value(deHoy))
        .andExpect(jsonPath("$.actividades[0].titulo").value("Mi mañana"))
        .andExpect(jsonPath("$.actividades[0].tipoActividad.codigo").value("ORDENAR"))
        .andExpect(jsonPath("$.actividades[0].tipoActividad.nombre").value("Ordenar la historia"))
        .andExpect(jsonPath("$.actividades[0].imagenUrl").value("https://picsum.photos/seed/rutina-portada/400/600"))
        .andExpect(jsonPath("$.actividades[0].fechaAsignacion").value(hoy.toString()))
        .andExpect(jsonPath("$.actividades[0].esHoy").value(true))
        .andExpect(jsonPath("$.actividades[0].estado").value("Pendiente"))
        .andExpect(jsonPath("$.actividades[0].realizada").value(false))
        // tareas 1 (08:45) y 2 (08:30) del mismo dia: primero la de hora mas tarde
        .andExpect(jsonPath("$.actividades[1].tareaId").value(1))
        .andExpect(jsonPath("$.actividades[1].estado").value("Realizada"))
        .andExpect(jsonPath("$.actividades[1].realizada").value(true))
        .andExpect(jsonPath("$.actividades[1].esHoy").value(false))
        .andExpect(jsonPath("$.actividades[2].tareaId").value(2))
        .andExpect(jsonPath("$.actividades[2].tipoActividad.codigo").value("UNIR_FLECHAS"));
  }

  // materia sin tareas: lista vacia y mensaje para el front
  @Test
  void materiaSinTareas_devuelveListaVaciaYMensaje() throws Exception {
    mockMvc.perform(get(URL_ACTIVIDADES, 5).header("Authorization", bearer(MARIEL)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.actividades", hasSize(0)))
        .andExpect(jsonPath("$.mensaje").value("Todavía no hay actividades en esta materia"));
  }

  // materia de la carpeta de Tomas: 403
  @Test
  void materiaDeOtroAlumno_devuelve403() throws Exception {
    mockMvc.perform(get(URL_ACTIVIDADES, 6).header("Authorization", bearer(MARIEL)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("No tienes permiso para ver esta carpeta"));
  }

  // materia que no existe: 404
  @Test
  void materiaInexistente_devuelve404() throws Exception {
    mockMvc.perform(get(URL_ACTIVIDADES, 999).header("Authorization", bearer(MARIEL)))
        .andExpect(status().isNotFound());
  }

  // sin token: 401 con el mensaje del entry point de SecurityConfig
  @Test
  void sinToken_devuelve401() throws Exception {
    mockMvc.perform(get(URL_ACTIVIDADES, 3))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Debes iniciar sesión"));
  }

  // un docente no es estudiante: 403 por rol
  @Test
  void usuarioQueNoEsEstudiante_devuelve403() throws Exception {
    mockMvc.perform(get(URL_ACTIVIDADES, 3).header("Authorization", bearer(DOCENTE)))
        .andExpect(status().isForbidden());
  }

  // endpoint de la carpeta (mismo controller): devuelve la carpeta 2026 de Mariel con sus 3 materias
  @Test
  void miCarpeta_devuelveLaCarpetaDelCicloActual() throws Exception {
    mockMvc.perform(get("/api/v1/mi-carpeta").header("Authorization", bearer(MARIEL)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nombre").value("Carpeta Rosa"))
        .andExpect(jsonPath("$.materias", hasSize(3)));
  }
}
