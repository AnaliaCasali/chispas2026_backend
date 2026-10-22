package edu.ies63.chispas2026_backend.controllers;

import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// TSDS-13: test de integracion del endpoint de entrega (app completa + MySQL con chispas_bd.sql).
// Usa los datos de ejemplo: Mariel (DNI 45123456) con Lengua = carpetaEspacio 3, Tomas tiene
// carpetaEspacio 6, y la actividad 4 de UNIR_FLECHAS con sus items y pares.
// El body de la entrega se arma leyendo los items y pares de la base, para no depender de ids fijos.
// Cada test corre en una transaccion que se deshace al final.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TareaControllerTest {

  private static final String URL_ENTREGA = "/api/v1/tareas/{id}/entrega";
  private static final String URL_RESULTADO = "/api/v1/tareas/{id}/resultado";
  private static final String MARIEL = "45123456";
  private static final long ACTIVIDAD_UNIR = 4L;   // UNIR_FLECHAS con items y pares de ejemplo
  private static final long LENGUA_MARIEL = 3L;
  private static final long MATERIA_TOMAS = 6L;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JdbcTemplate jdbc;

  // para forzar el flush: la transaccion del test recien escribe en la base
  // al final, y el jdbc no ve los cambios pendientes del persistence context
  @Autowired
  private EntityManager entityManager;

  // hace login con el endpoint real y devuelve el header Authorization
  private String bearer(String usuario) throws Exception {
    String body = mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"%s\",\"password\":\"Chispas2026\"}".formatted(usuario)))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    return "Bearer " + JsonPath.read(body, "$.token");
  }

  // fecha fija vieja: la entrega no depende de la fecha y asi el insert
  // no choca con la restriccion unica si ya hay tareas de hoy en esa materia
  private long insertarTarea(long actividadId, long carpetaEspacioId) {
    jdbc.update("""
        INSERT INTO tarea (actividad_id, carpeta_espacio_id, asignada_por, fecha_asignacion, asignada_en, estado)
        VALUES (?, ?, 1, '2024-06-15', NOW(), 'PENDIENTE')""", actividadId, carpetaEspacioId);
    return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
  }

  // los pares correctos de la actividad, leidos de la base (item izq -> item der)
  private Map<Long, Long> paresDeLaActividad() {
    Map<Long, Long> pares = new HashMap<>();
    for (Map<String, Object> fila : jdbc.queryForList(
        "SELECT item_izq_id, item_der_id FROM actividad_par WHERE actividad_id = ?", ACTIVIDAD_UNIR)) {
      pares.put(((Number) fila.get("item_izq_id")).longValue(),
          ((Number) fila.get("item_der_id")).longValue());
    }
    return pares;
  }

  // los items que el alumno responde: los de la columna izquierda
  private List<Long> itemsIzquierdos() {
    return jdbc.queryForList(
        "SELECT id FROM actividad_imagen WHERE actividad_id = ? AND lado = 'IZQ' ORDER BY nro_orden",
        Long.class, ACTIVIDAD_UNIR);
  }

  // total de items a responder (los IZQ de la actividad)
  private int totalDeLaActividad() {
    return itemsIzquierdos().size();
  }

  // arma el body de la entrega: un item por cada IZQ unido a su par correcto.
  // los primeros "cruzados" se unen al der de otro par para que cuenten mal
  private String bodyDeEntrega(int cruzados) {
    List<Long> izquierdos = itemsIzquierdos();
    Map<Long, Long> pares = paresDeLaActividad();
    List<Long> ders = new ArrayList<>(pares.values());

    StringBuilder items = new StringBuilder();
    for (int i = 0; i < izquierdos.size(); i++) {
      Long izq = izquierdos.get(i);
      Long der = pares.getOrDefault(izq, ders.get(i % ders.size()));
      if (i < cruzados) {
        // el der de otro par: distinto del propio porque actividad_par no repite der
        der = ders.stream().filter(d -> !d.equals(pares.get(izq))).findFirst().orElse(der);
      }
      items.append(String.format("{\"itemId\":%d,\"itemUnidoId\":%d}%s",
          izq, der, i < izquierdos.size() - 1 ? "," : ""));
    }
    return "{\"items\":[" + items + "]}";
  }

  // mismo tamanio que la actividad pero el segundo item repite al primero
  private String bodyConItemRepetido() {
    List<Long> izquierdos = itemsIzquierdos();
    Map<Long, Long> pares = paresDeLaActividad();
    List<Long> ders = new ArrayList<>(pares.values());

    StringBuilder items = new StringBuilder();
    for (int i = 0; i < izquierdos.size(); i++) {
      Long izq = i == 1 ? izquierdos.get(0) : izquierdos.get(i);
      Long der = pares.getOrDefault(izq, ders.get(i % ders.size()));
      items.append(String.format("{\"itemId\":%d,\"itemUnidoId\":%d}%s",
          izq, der, i < izquierdos.size() - 1 ? "," : ""));
    }
    return "{\"items\":[" + items + "]}";
  }

  // ---------- entregas correctas y reintentos ----------

  // entrega con todos los pares correctos: felicitaciones, FINALIZADA y respuestas del intento 1
  @Test
  void entregaConParesCorrectos_felicitacionesYRealizada() throws Exception {
    long tareaId = insertarTarea(ACTIVIDAD_UNIR, LENGUA_MARIEL);

    mockMvc.perform(post(URL_ENTREGA, tareaId).header("Authorization", bearer(MARIEL))
            .contentType(MediaType.APPLICATION_JSON)
            .content(bodyDeEntrega(0)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tareaId").value(tareaId))
        .andExpect(jsonPath("$.aciertos").value(totalDeLaActividad()))
        .andExpect(jsonPath("$.totalItems").value(totalDeLaActividad()))
        .andExpect(jsonPath("$.mensaje").value("¡Felicitaciones respondiste todo correctamente!"))
        .andExpect(jsonPath("$.estado").value("Realizada"))
        .andExpect(jsonPath("$.puedeIntentar").value(false));

    // en la base quedo finalizada con una respuesta por item del intento 1
    // (el flush escribe en la conexion los cambios pendientes de la entrega)
    entityManager.flush();
    assertEquals("FINALIZADA", jdbc.queryForObject(
        "SELECT estado FROM tarea WHERE id = ?", String.class, tareaId));
    assertEquals(totalDeLaActividad(), jdbc.queryForObject(
        "SELECT COUNT(*) FROM respuesta WHERE tarea_id = ? AND intento = 1", Integer.class, tareaId));
  }

  // una union cruzada: muy cerca, EN_CURSO y puede reintentar
  @Test
  void entregaConUnaUnionCruzada_muyCercaYQuedaEnCurso() throws Exception {
    long tareaId = insertarTarea(ACTIVIDAD_UNIR, LENGUA_MARIEL);

    mockMvc.perform(post(URL_ENTREGA, tareaId).header("Authorization", bearer(MARIEL))
            .contentType(MediaType.APPLICATION_JSON)
            .content(bodyDeEntrega(1)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.aciertos").value(totalDeLaActividad() - 1))
        .andExpect(jsonPath("$.mensaje").value("¡Estuviste muy cerca! ¡Inténtalo de nuevo!"))
        .andExpect(jsonPath("$.estado").value("Pendiente"))
        .andExpect(jsonPath("$.puedeIntentar").value(true));

    entityManager.flush();
    assertEquals("EN_CURSO", jdbc.queryForObject(
        "SELECT estado FROM tarea WHERE id = ?", String.class, tareaId));
  }

  // despues de acertar todo, otra entrega sobre la misma tarea: 409
  @Test
  void despuesDeAcertarTodo_segundaEntregaDa409() throws Exception {
    long tareaId = insertarTarea(ACTIVIDAD_UNIR, LENGUA_MARIEL);
    String bearer = bearer(MARIEL);

    mockMvc.perform(post(URL_ENTREGA, tareaId).header("Authorization", bearer)
            .contentType(MediaType.APPLICATION_JSON)
            .content(bodyDeEntrega(0)))
        .andExpect(status().isOk());

    mockMvc.perform(post(URL_ENTREGA, tareaId).header("Authorization", bearer)
            .contentType(MediaType.APPLICATION_JSON)
            .content(bodyDeEntrega(0)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("Ya respondiste correctamente esta actividad"));
  }

  // ---------- errores ----------

  // tarea de la carpeta de Tomas con el token de Mariel: 403
  @Test
  void tareaDeOtroAlumno_da403() throws Exception {
    long tareaId = insertarTarea(ACTIVIDAD_UNIR, MATERIA_TOMAS);

    mockMvc.perform(post(URL_ENTREGA, tareaId).header("Authorization", bearer(MARIEL))
            .contentType(MediaType.APPLICATION_JSON)
            .content(bodyDeEntrega(0)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("No tienes permiso para responder esta tarea"));
  }

  // tarea que no existe: 404
  @Test
  void tareaInexistente_da404() throws Exception {
    mockMvc.perform(post(URL_ENTREGA, 999).header("Authorization", bearer(MARIEL))
            .contentType(MediaType.APPLICATION_JSON)
            .content(bodyDeEntrega(0)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("La tarea solicitada no existe"));
  }

  // sin token: 401 con el mensaje del entry point de SecurityConfig
  @Test
  void sinToken_da401() throws Exception {
    mockMvc.perform(post(URL_ENTREGA, 1).contentType(MediaType.APPLICATION_JSON)
            .content(bodyDeEntrega(0)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Debes iniciar sesión"));
  }

  // items vacios: 400 de validacion
  @Test
  void itemsVacios_da400() throws Exception {
    long tareaId = insertarTarea(ACTIVIDAD_UNIR, LENGUA_MARIEL);

    mockMvc.perform(post(URL_ENTREGA, tareaId).header("Authorization", bearer(MARIEL))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"items\":[]}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Error de validación"));
  }

  // item repetido (mismo tamanio pero el segundo repite al primero): 400
  @Test
  void itemRepetido_da400() throws Exception {
    long tareaId = insertarTarea(ACTIVIDAD_UNIR, LENGUA_MARIEL);

    mockMvc.perform(post(URL_ENTREGA, tareaId).header("Authorization", bearer(MARIEL))
            .contentType(MediaType.APPLICATION_JSON)
            .content(bodyConItemRepetido()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Cada ítem de la actividad se responde una sola vez"));
  }

  // ---------- historial ----------

  // tarea recien asignada, sin entregas: 0 intentos y sin ultimo resultado
  @Test
  void resultadoSinEntregas_devuelveCeroIntentos() throws Exception {
    long tareaId = insertarTarea(ACTIVIDAD_UNIR, LENGUA_MARIEL);

    mockMvc.perform(get(URL_RESULTADO, tareaId).header("Authorization", bearer(MARIEL)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tareaId").value(tareaId))
        .andExpect(jsonPath("$.cantidadIntentos").value(0))
        .andExpect(jsonPath("$.intentoActual").value(0))
        .andExpect(jsonPath("$.ultimoResultado").doesNotExist())  // en el JSON viaja null
        .andExpect(jsonPath("$.intentos").isEmpty());
  }

  // dos entregas: el ultimo resultado y el contador de intentos
  @Test
  void resultadoDespuesDeDosEntregas_devuelveElUltimoYElContador() throws Exception {
    long tareaId = insertarTarea(ACTIVIDAD_UNIR, LENGUA_MARIEL);
    String bearer = bearer(MARIEL);

    // intento 1: una union cruzada (queda en curso, puede reintentar)
    mockMvc.perform(post(URL_ENTREGA, tareaId).header("Authorization", bearer)
            .contentType(MediaType.APPLICATION_JSON)
            .content(bodyDeEntrega(1)))
        .andExpect(status().isOk());

    // intento 2: todo bien (finaliza)
    mockMvc.perform(post(URL_ENTREGA, tareaId).header("Authorization", bearer)
            .contentType(MediaType.APPLICATION_JSON)
            .content(bodyDeEntrega(0)))
        .andExpect(status().isOk());

    mockMvc.perform(get(URL_RESULTADO, tareaId).header("Authorization", bearer))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tareaId").value(tareaId))
        .andExpect(jsonPath("$.cantidadIntentos").value(2))
        .andExpect(jsonPath("$.intentoActual").value(2))
        .andExpect(jsonPath("$.ultimoResultado.aciertos").value(totalDeLaActividad()))
        .andExpect(jsonPath("$.ultimoResultado.totalItems").value(totalDeLaActividad()))
        .andExpect(jsonPath("$.ultimoResultado.mensaje")
            .value("¡Felicitaciones respondiste todo correctamente!"))
        .andExpect(jsonPath("$.ultimoResultado.puedeIntentar").value(false))
        .andExpect(jsonPath("$.intentos[0].intento").value(1))
        .andExpect(jsonPath("$.intentos[0].aciertos").value(totalDeLaActividad() - 1))
        .andExpect(jsonPath("$.intentos[0].totalItems").value(totalDeLaActividad()))
        .andExpect(jsonPath("$.intentos[1].intento").value(2))
        .andExpect(jsonPath("$.intentos[1].aciertos").value(totalDeLaActividad()));

    // en la base quedaron los 2 intentos guardados (el historial no se pisa)
    entityManager.flush();
    assertEquals(2, jdbc.queryForObject(
        "SELECT COUNT(DISTINCT intento) FROM respuesta WHERE tarea_id = ?", Integer.class, tareaId));
  }

  // tarea de la carpeta de Tomas con el token de Mariel: 403
  @Test
  void resultadoDeOtroAlumno_da403() throws Exception {
    long tareaId = insertarTarea(ACTIVIDAD_UNIR, MATERIA_TOMAS);

    mockMvc.perform(get(URL_RESULTADO, tareaId).header("Authorization", bearer(MARIEL)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("No tienes permiso para responder esta tarea"));
  }

  // tarea que no existe: 404
  @Test
  void resultadoTareaInexistente_da404() throws Exception {
    mockMvc.perform(get(URL_RESULTADO, 999).header("Authorization", bearer(MARIEL)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("La tarea solicitada no existe"));
  }

  // sin token: 401 con el mensaje del entry point de SecurityConfig
  @Test
  void resultadoSinToken_da401() throws Exception {
    mockMvc.perform(get(URL_RESULTADO, 1))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Debes iniciar sesión"));
  }
}