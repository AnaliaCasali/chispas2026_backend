package edu.ies63.chispas2026_backend.services.impl;

import edu.ies63.chispas2026_backend.dtos.EstudianteResumenDTO;
import edu.ies63.chispas2026_backend.dtos.PerfilEstudianteDTO;
import edu.ies63.chispas2026_backend.enums.Role;
import edu.ies63.chispas2026_backend.exceptions.AccesoDenegadoException;
import edu.ies63.chispas2026_backend.models.*;
import edu.ies63.chispas2026_backend.repositories.CarpetaRepository;
import edu.ies63.chispas2026_backend.repositories.EstudianteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

// tests unitarios del perfil del estudiante con los repositorios mockeados
class EstudianteServiceImplTest {

  private final EstudianteRepository estudianteRepository = mock(EstudianteRepository.class);
  private final CarpetaRepository carpetaRepository = mock(CarpetaRepository.class);
  private final EstudianteServiceImpl service = new EstudianteServiceImpl(estudianteRepository, carpetaRepository);

  private final Usuario laura = Usuario.builder().id(1L).nombre("Laura").role(Role.DOCENTE).build();
  private final Usuario usuarioMariel = Usuario.builder().id(2L).nombre("Mariel").apellido("Linera")
      .fotoUrl("https://img/mariel.jpg").role(Role.ESTUDIANTE).build();
  private final Curso cuartoA = Curso.builder().id(1L).nombre("4to Año").division("A").build();
  private Estudiante mariel;

  @BeforeEach
  void setUp() {
    mariel = Estudiante.builder().id(2L).usuario(usuarioMariel).build();
    when(estudianteRepository.findConCondicionesById(2L)).thenReturn(Optional.of(mariel));
    when(carpetaRepository.findByEstudianteIdAndCicloLectivoActivoTrue(2L))
        .thenReturn(Optional.of(Carpeta.builder().id(2L).estudiante(mariel).curso(cuartoA).build()));
    when(carpetaRepository.esAlumnoDelDocente(2L, 1L)).thenReturn(true);
  }

  private void conCondiciones(String... nombres) {
    long id = 1;
    for (String nombre : nombres) {
      mariel.getCondiciones().add(Condicion.builder().id(id++).nombre(nombre).build());
    }
  }

  // ---------- alumno ----------

  // escenario 2: el alumno ve su perfil con nombre, curso y condiciones
  @Test
  void alumno_veSuPropioPerfil() {
    conCondiciones("TDA");

    PerfilEstudianteDTO perfil = service.obtenerPerfil(usuarioMariel, 2L);

    assertThat(perfil.id()).isEqualTo(2L);
    assertThat(perfil.nombre()).isEqualTo("Mariel");
    assertThat(perfil.apellido()).isEqualTo("Linera");
    assertThat(perfil.fotoUrl()).isEqualTo("https://img/mariel.jpg");
    assertThat(perfil.curso()).isEqualTo("4to Año A");
    assertThat(perfil.condiciones()).containsExactly("TDA");
    assertThat(perfil.mensaje()).isNull();
    // al alumno no se le chequea asignacion de docente
    verify(carpetaRepository, never()).esAlumnoDelDocente(anyLong(), anyLong());
  }

  // escenario 5 (alumno): otro perfil se deniega sin consultar si existe
  @Test
  void alumno_noPuedeVerOtroPerfil() {
    assertThatThrownBy(() -> service.obtenerPerfil(usuarioMariel, 3L))
        .isInstanceOf(AccesoDenegadoException.class)
        .hasMessage("No tienes permiso para ver este perfil");
    verifyNoInteractions(estudianteRepository);
  }

  // ---------- docente ----------

  // escenario 1: el docente ve el perfil de un alumno asignado
  @Test
  void docente_veElPerfilDeSuAlumno() {
    conCondiciones("Dislexia");

    PerfilEstudianteDTO perfil = service.obtenerPerfil(laura, 2L);

    assertThat(perfil.nombre()).isEqualTo("Mariel");
    assertThat(perfil.curso()).isEqualTo("4to Año A");
    assertThat(perfil.condiciones()).containsExactly("Dislexia");
  }

  // escenario 5 (docente): alumno que no es suyo
  @Test
  void docente_alumnoNoAsignado_deniegaAcceso() {
    when(carpetaRepository.esAlumnoDelDocente(2L, 1L)).thenReturn(false);

    assertThatThrownBy(() -> service.obtenerPerfil(laura, 2L))
        .isInstanceOf(AccesoDenegadoException.class)
        .hasMessage("No tienes permiso para ver este perfil");
  }

  @Test
  void estudianteInexistente_lanzaNoEncontrado() {
    when(estudianteRepository.findConCondicionesById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.obtenerPerfil(laura, 99L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("El estudiante no existe");
  }

  // ---------- condiciones y curso ----------

  // escenario 3: sin condiciones va lista vacia (nunca null) y el mensaje
  @Test
  void sinCondiciones_listaVaciaYMensaje() {
    PerfilEstudianteDTO perfil = service.obtenerPerfil(laura, 2L);

    assertThat(perfil.condiciones()).isNotNull().isEmpty();
    assertThat(perfil.mensaje()).isEqualTo("Sin condición registrada");
  }

  // escenario 4: muestra todas, en orden alfabetico para que no cambie entre llamadas
  @Test
  void variasCondiciones_lasMuestraTodasOrdenadas() {
    conCondiciones("TDA", "Dislexia", "Autismo");

    PerfilEstudianteDTO perfil = service.obtenerPerfil(laura, 2L);

    assertThat(perfil.condiciones()).containsExactly("Autismo", "Dislexia", "TDA");
    assertThat(perfil.mensaje()).isNull();
  }

  // sin carpeta en el ciclo actual el perfil se muestra igual, sin curso
  @Test
  void sinCarpetaEnElCicloActual_cursoNull() {
    when(carpetaRepository.findByEstudianteIdAndCicloLectivoActivoTrue(2L)).thenReturn(Optional.empty());

    assertThat(service.obtenerPerfil(usuarioMariel, 2L).curso()).isNull();
  }

  @Test
  void cursoSinDivision_muestraSoloElNombre() {
    cuartoA.setDivision(null);

    assertThat(service.obtenerPerfil(usuarioMariel, 2L).curso()).isEqualTo("4to Año");
  }

  // ---------- lista del docente ----------

  @Test
  void listaDelDocente_devuelveSusAlumnosConCurso() {
    Usuario usuarioTomas = Usuario.builder().id(3L).nombre("Tomás").apellido("Ríos").build();
    Estudiante tomas = Estudiante.builder().id(3L).usuario(usuarioTomas).build();
    when(carpetaRepository.findDelCicloActivoPorDocente(1L)).thenReturn(List.of(
        Carpeta.builder().estudiante(mariel).curso(cuartoA).build(),
        Carpeta.builder().estudiante(tomas).curso(cuartoA).build()
    ));

    List<EstudianteResumenDTO> alumnos = service.listarMisAlumnos(laura);

    assertThat(alumnos).containsExactly(
        new EstudianteResumenDTO(2L, "Mariel", "Linera", "4to Año A"),
        new EstudianteResumenDTO(3L, "Tomás", "Ríos", "4to Año A"));
  }

  @Test
  void listaDelDocente_sinAlumnos_listaVacia() {
    when(carpetaRepository.findDelCicloActivoPorDocente(1L)).thenReturn(List.of());

    assertThat(service.listarMisAlumnos(laura)).isEmpty();
  }

  // el set de condiciones arranca vacio, no null
  @Test
  void estudianteNuevo_tieneCondicionesVacias() {
    assertThat(Estudiante.builder().build().getCondiciones()).isEqualTo(Set.of());
  }
}
