package edu.ies63.chispas2026_backend.repositories;

import edu.ies63.chispas2026_backend.models.Carpeta;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// prueba las consultas de alumnos del docente contra MySQL real (chispas_bd.sql).
// Laura (1) da Lengua en 4to A (Mariel 2 y Tomás 3). Pablo (4) da Lengua en 5to B (Lucía 5).
// Cada test corre en una transaccion que se deshace al final.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CarpetaRepositoryTest {

  private static final long LAURA = 1L;
  private static final long PABLO = 4L;

  @Autowired
  private CarpetaRepository carpetaRepository;

  @Autowired
  private EstudianteRepository estudianteRepository;

  @Autowired
  private JdbcTemplate jdbc;

  private List<Long> idsDeAlumnos(List<Carpeta> carpetas) {
    return carpetas.stream().map(c -> c.getEstudiante().getId()).toList();
  }

  @Test
  void carpetaDelCicloActivo_traeLaDe2026ConSuCurso() {
    Carpeta carpeta = carpetaRepository.findByEstudianteIdAndCicloLectivoActivoTrue(2L).orElseThrow();

    assertThat(carpeta.getId()).isEqualTo(2L);
    assertThat(carpeta.getCurso().getNombre()).isEqualTo("4to Año");
  }

  @Test
  void esAlumnoDelDocente_soloSiDaClaseEnSuCurso() {
    assertThat(carpetaRepository.esAlumnoDelDocente(2L, LAURA)).isTrue();
    assertThat(carpetaRepository.esAlumnoDelDocente(3L, LAURA)).isTrue();
    assertThat(carpetaRepository.esAlumnoDelDocente(5L, LAURA)).isFalse();
    assertThat(carpetaRepository.esAlumnoDelDocente(5L, PABLO)).isTrue();
    assertThat(carpetaRepository.esAlumnoDelDocente(2L, PABLO)).isFalse();
  }

  // Mariel estuvo en 3er Año A en 2025 (curso 2): si Pablo tuviera horario ahi no cuenta, no es del ciclo activo
  @Test
  void esAlumnoDelDocente_ignoraCursosDeCiclosAnteriores() {
    jdbc.update("""
        INSERT INTO horario (curso_id, espacio_curricular_id, docente_id, dia, hora_inicio, hora_fin)
        VALUES (2, 1, 4, 'MARTES', '08:00', '09:00')""");

    assertThat(carpetaRepository.esAlumnoDelDocente(2L, PABLO)).isFalse();
  }

  @Test
  void alumnosDelDocente_ordenadosPorApellidoConCursoCargado() {
    List<Carpeta> carpetas = carpetaRepository.findDelCicloActivoPorDocente(LAURA);

    // Linera, Ríos
    assertThat(idsDeAlumnos(carpetas)).containsExactly(2L, 3L);
    assertThat(carpetas).allSatisfy(c -> {
      assertThat(Hibernate.isInitialized(c.getCurso())).isTrue();
      assertThat(Hibernate.isInitialized(c.getEstudiante().getUsuario())).isTrue();
    });
  }

  // aunque de varias materias en el mismo curso, cada alumno aparece una sola vez
  @Test
  void alumnosDelDocente_sinRepetidosAunqueDeVariasMaterias() {
    jdbc.update("UPDATE horario SET docente_id = 1 WHERE curso_id = 1");

    assertThat(idsDeAlumnos(carpetaRepository.findDelCicloActivoPorDocente(LAURA))).containsExactly(2L, 3L);
  }

  @Test
  void alumnosDelDocente_otroDocenteVeSoloLosSuyos() {
    assertThat(idsDeAlumnos(carpetaRepository.findDelCicloActivoPorDocente(PABLO))).containsExactly(5L);
  }

  @Test
  void docenteSinHorarios_noTieneAlumnos() {
    assertThat(carpetaRepository.findDelCicloActivoPorDocente(2L)).isEmpty();
  }

  @Test
  void estudianteConCondiciones_lasTraeCargadas() {
    var mariel = estudianteRepository.findConCondicionesById(2L).orElseThrow();

    assertThat(Hibernate.isInitialized(mariel.getCondiciones())).isTrue();
    assertThat(mariel.getCondiciones()).extracting("nombre").containsExactlyInAnyOrder("TDA", "Dislexia");
    assertThat(estudianteRepository.findConCondicionesById(3L).orElseThrow().getCondiciones()).isEmpty();
  }
}
