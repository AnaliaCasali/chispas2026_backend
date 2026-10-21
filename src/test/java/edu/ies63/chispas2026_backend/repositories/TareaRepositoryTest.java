package edu.ies63.chispas2026_backend.repositories;

import edu.ies63.chispas2026_backend.models.Tarea;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// TSDS-4: prueba la consulta findAsignadasHastaHoy contra MySQL real (chispas_bd.sql).
// Trabaja sobre Matematica de Mariel (carpetaEspacio 4), que no tiene tareas de ejemplo.
// Cada test corre en una transaccion que se deshace al final.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TareaRepositoryTest {

  private static final long MATEMATICA_MARIEL = 4L;
  private static final LocalDate HOY = LocalDate.of(2026, 10, 20);

  @Autowired
  private TareaRepository tareaRepository;

  @Autowired
  private JdbcTemplate jdbc;

  // TSDS-13: para probar maxIntentoPorTarea
  @Autowired
  private RespuestaRepository respuestaRepository;

  // inserta una tarea en la carpetaEspacio indicada y devuelve su id
  private long insertarTarea(long carpetaEspacioId, long actividadId, String fecha, String hora) {
    return insertarTarea(carpetaEspacioId, actividadId, fecha, hora, "PENDIENTE");
  }

  // TSDS-6: igual que la anterior pero con el estado que se pida
  private long insertarTarea(long carpetaEspacioId, long actividadId, String fecha, String hora, String estado) {
    jdbc.update("""
        INSERT INTO tarea (actividad_id, carpeta_espacio_id, asignada_por, fecha_asignacion, asignada_en, estado)
        VALUES (?, ?, 1, ?, ?, ?)""", actividadId, carpetaEspacioId, fecha, fecha + " " + hora, estado);
    return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
  }

  private List<Long> idsDe(List<Tarea> tareas) {
    return tareas.stream().map(Tarea::getId).toList();
  }

  // mismo dia: ordena por hora de asignacion, no por id
  // (la de 11:00 se inserta primero, asi tiene id menor y el orden por id daria al reves)
  @Test
  void ordenaPorFechaYDespuesPorHoraDeAsignacion() {
    long tarde = insertarTarea(MATEMATICA_MARIEL, 2, "2026-10-19", "11:00:00");
    long temprano = insertarTarea(MATEMATICA_MARIEL, 1, "2026-10-19", "09:00:00");
    long diaAnterior = insertarTarea(MATEMATICA_MARIEL, 3, "2026-10-18", "15:00:00");

    List<Tarea> tareas = tareaRepository.findAsignadasHastaHoy(MATEMATICA_MARIEL, HOY);

    assertThat(idsDe(tareas)).containsExactly(tarde, temprano, diaAnterior);
  }

  // misma fecha y misma hora: desempata por id (la ultima insertada primero)
  @Test
  void conMismaHoraDesempataPorId() {
    long primera = insertarTarea(MATEMATICA_MARIEL, 1, "2026-10-19", "09:00:00");
    long segunda = insertarTarea(MATEMATICA_MARIEL, 2, "2026-10-19", "09:00:00");

    List<Tarea> tareas = tareaRepository.findAsignadasHastaHoy(MATEMATICA_MARIEL, HOY);

    assertThat(idsDe(tareas)).containsExactly(segunda, primera);
  }

  // incluye las de hoy y excluye las de fechas futuras
  @Test
  void incluyeLasDeHoyYExcluyeLasFuturas() {
    long deHoy = insertarTarea(MATEMATICA_MARIEL, 1, "2026-10-20", "23:59:00");
    insertarTarea(MATEMATICA_MARIEL, 2, "2026-10-21", "00:01:00");

    List<Tarea> tareas = tareaRepository.findAsignadasHastaHoy(MATEMATICA_MARIEL, HOY);

    assertThat(idsDe(tareas)).containsExactly(deHoy);
  }

  // solo trae las de esa materia de esa carpeta (no las de Lengua ni las de Tomas)
  @Test
  void soloTraeTareasDeLaCarpetaEspacioPedida() {
    long propia = insertarTarea(MATEMATICA_MARIEL, 1, "2026-10-19", "09:00:00");
    insertarTarea(7L, 1, "2026-10-19", "09:00:00"); // Matematica de Tomas

    List<Tarea> tareas = tareaRepository.findAsignadasHastaHoy(MATEMATICA_MARIEL, HOY);

    assertThat(idsDe(tareas)).containsExactly(propia);
  }

  // trae actividad, plantilla y contenido en la misma consulta (sin N+1)
  @Test
  void traeActividadPlantillaYContenidoCargados() {
    insertarTarea(MATEMATICA_MARIEL, 3, "2026-10-19", "09:00:00");

    Tarea tarea = tareaRepository.findAsignadasHastaHoy(MATEMATICA_MARIEL, HOY).get(0);

    assertThat(Hibernate.isInitialized(tarea.getActividad())).isTrue();
    assertThat(Hibernate.isInitialized(tarea.getActividad().getPlantilla())).isTrue();
    assertThat(Hibernate.isInitialized(tarea.getActividad().getContenido())).isTrue();
    assertThat(tarea.getActividad().getPlantilla().getCodigo().name()).isEqualTo("UNIR_FLECHAS");
    assertThat(tarea.getAsignadaEn()).isEqualTo("2026-10-19T09:00:00");
  }

  // ---- TSDS-6: findPendientesHastaHoy (toda la carpeta 2 de Mariel) ----

  private static final long CARPETA_MARIEL = 2L;
  private static final long TAREA_PENDIENTE_DE_LENGUA = 2L; // dato de ejemplo (la 1 esta FINALIZADA)

  // trae PENDIENTE y EN_CURSO de todas sus materias, deja afuera FINALIZADA
  @Test
  void pendientes_incluyeEnCursoYExcluyeFinalizadas() {
    long pendiente = insertarTarea(MATEMATICA_MARIEL, 1, "2026-10-19", "09:00:00", "PENDIENTE");
    long enCurso = insertarTarea(MATEMATICA_MARIEL, 2, "2026-10-18", "09:00:00", "EN_CURSO");
    insertarTarea(MATEMATICA_MARIEL, 3, "2026-10-17", "09:00:00", "FINALIZADA");

    List<Tarea> tareas = tareaRepository.findPendientesHastaHoy(CARPETA_MARIEL, HOY);

    assertThat(idsDe(tareas)).containsExactly(pendiente, enCurso, TAREA_PENDIENTE_DE_LENGUA);
  }

  // mismo orden que la TSDS-4: fecha, hora de asignacion y id
  @Test
  void pendientes_ordenaPorFechaYHoraDeAsignacion() {
    long tarde = insertarTarea(MATEMATICA_MARIEL, 2, "2026-10-19", "11:00:00");
    long temprano = insertarTarea(MATEMATICA_MARIEL, 1, "2026-10-19", "09:00:00");
    long deHoy = insertarTarea(5L, 1, "2026-10-20", "08:00:00"); // Historia

    List<Tarea> tareas = tareaRepository.findPendientesHastaHoy(CARPETA_MARIEL, HOY);

    assertThat(idsDe(tareas)).containsExactly(deHoy, tarde, temprano, TAREA_PENDIENTE_DE_LENGUA);
  }

  // no trae las futuras ni las de otra carpeta (Tomas), y viene con lo necesario cargado
  @Test
  void pendientes_excluyeFuturasYOtrasCarpetas() {
    insertarTarea(MATEMATICA_MARIEL, 1, "2026-10-21", "08:00:00");
    insertarTarea(7L, 1, "2026-10-19", "09:00:00"); // Matematica de Tomas

    List<Tarea> tareas = tareaRepository.findPendientesHastaHoy(CARPETA_MARIEL, HOY);

    assertThat(idsDe(tareas)).containsExactly(TAREA_PENDIENTE_DE_LENGUA);
    assertThat(Hibernate.isInitialized(tareas.get(0).getActividad().getPlantilla())).isTrue();
    assertThat(Hibernate.isInitialized(tareas.get(0).getActividad().getContenido())).isTrue();
  }

  // ---- TSDS-13: findCompletaById y maxIntentoPorTarea ----

  // trae la tarea con actividad, plantilla y la carpeta del alumno cargadas (sin N+1)
  @Test
  void findCompletaByIdTraeTodoCargado() {
    long tareaId = insertarTarea(MATEMATICA_MARIEL, 3, "2026-10-19", "09:00:00");

    Tarea tarea = tareaRepository.findCompletaById(tareaId).orElseThrow();

    assertThat(Hibernate.isInitialized(tarea.getActividad())).isTrue();
    assertThat(Hibernate.isInitialized(tarea.getActividad().getPlantilla())).isTrue();
    assertThat(Hibernate.isInitialized(tarea.getCarpetaEspacio())).isTrue();
    assertThat(Hibernate.isInitialized(tarea.getCarpetaEspacio().getCarpeta())).isTrue();
    assertThat(Hibernate.isInitialized(tarea.getCarpetaEspacio().getCarpeta().getEstudiante())).isTrue();
    assertThat(Hibernate.isInitialized(tarea.getCarpetaEspacio().getCarpeta().getEstudiante().getUsuario())).isTrue();
  }

  // sin respuestas da 0, con historial da el ultimo intento guardado
  @Test
  void maxIntentoPorTarea_daCeroSinRespuestasYElUltimoConHistorial() {
    long tareaId = insertarTarea(MATEMATICA_MARIEL, 2, "2026-10-19", "09:00:00");
    long itemId = jdbc.queryForObject(
        "SELECT id FROM actividad_imagen WHERE actividad_id = 2 LIMIT 1", Long.class);

    assertThat(respuestaRepository.maxIntentoPorTarea(tareaId)).isZero();

    insertarRespuesta(tareaId, itemId, 1);
    insertarRespuesta(tareaId, itemId, 2);

    assertThat(respuestaRepository.maxIntentoPorTarea(tareaId)).isEqualTo(2);
  }

  // inserta una respuesta del intento indicado (incorrecta, para el max no importa)
  private void insertarRespuesta(long tareaId, long itemId, int intento) {
    jdbc.update("""
        INSERT INTO respuesta (tarea_id, item_id, intento, es_correcta)
        VALUES (?, ?, ?, false)""", tareaId, itemId, intento);
  }
}
