package edu.ies63.chispas2026_backend.services.impl;

import edu.ies63.chispas2026_backend.dtos.ClaseHorarioDTO;
import edu.ies63.chispas2026_backend.dtos.MiHorarioDTO;
import edu.ies63.chispas2026_backend.enums.DiaSemana;
import edu.ies63.chispas2026_backend.models.*;
import edu.ies63.chispas2026_backend.repositories.CarpetaEspacioRepository;
import edu.ies63.chispas2026_backend.repositories.CarpetaRepository;
import edu.ies63.chispas2026_backend.repositories.HorarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// pantalla Horarios: clases de hoy del curso, solo de las materias de la carpeta, con la de ahora marcada
class HorarioServiceImplTest {

  private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");

  private final CarpetaRepository carpetaRepository = mock(CarpetaRepository.class);
  private final CarpetaEspacioRepository carpetaEspacioRepository = mock(CarpetaEspacioRepository.class);
  private final HorarioRepository horarioRepository = mock(HorarioRepository.class);

  private Usuario mariel;
  private EspacioCurricular espLengua, espMatematica, espHistoria;

  @BeforeEach
  void setUp() {
    mariel = Usuario.builder().id(2L).build();
    Estudiante estudiante = Estudiante.builder().id(2L).usuario(mariel).build();
    Curso curso = Curso.builder().id(1L).build();
    Carpeta carpeta = Carpeta.builder().id(2L).estudiante(estudiante).curso(curso).nombre("Carpeta Rosa").build();

    espLengua = EspacioCurricular.builder().id(1L).nombre("Lengua y Literatura").color("#4F7BE8").icono("libro.svg").build();
    espMatematica = EspacioCurricular.builder().id(2L).nombre("Matemática").color("#E85F7B").build();
    espHistoria = EspacioCurricular.builder().id(3L).nombre("Historia").color("#C08A3E").build();

    when(carpetaRepository.findByEstudianteIdAndCicloLectivoActivoTrue(2L)).thenReturn(Optional.of(carpeta));
    // Mariel cursa Lengua y Matematica, Historia no esta en su carpeta
    when(carpetaEspacioRepository.findByCarpetaIdOrderByOrdenAsc(2L)).thenReturn(List.of(
        CarpetaEspacio.builder().id(3L).carpeta(carpeta).espacioCurricular(espLengua).orden(1).build(),
        CarpetaEspacio.builder().id(4L).carpeta(carpeta).espacioCurricular(espMatematica).orden(2).build()));
    // los lunes: lengua 08:45, mate 09:30 e historia 10:15
    when(horarioRepository.findByCursoIdAndDiaOrderByHoraInicioAsc(1L, DiaSemana.LUNES)).thenReturn(List.of(
        horario(espLengua, "08:45", "09:30"),
        horario(espMatematica, "09:30", "10:15"),
        horario(espHistoria, "10:15", "11:00")));
  }

  private Horario horario(EspacioCurricular espacio, String inicio, String fin) {
    return Horario.builder().espacioCurricular(espacio)
        .horaInicio(LocalTime.parse(inicio)).horaFin(LocalTime.parse(fin)).build();
  }

  private HorarioServiceImpl servicioA(String fechaHora) {
    Clock clock = Clock.fixed(LocalDateTime.parse(fechaHora).atZone(ZONA).toInstant(), ZONA);
    return new HorarioServiceImpl(carpetaRepository, carpetaEspacioRepository, horarioRepository, clock);
  }

  @Test
  void enHorarioDeClase_traeLasClasesDeHoyConLaDeAhoraMarcada() {
    // lunes 9:40, esta en matematica
    MiHorarioDTO dto = servicioA("2026-10-19T09:40").obtenerMiHorario(mariel);

    assertThat(dto.dia()).isEqualTo(DiaSemana.LUNES);
    assertThat(dto.mensaje()).isNull();
    assertThat(dto.clases()).extracting(ClaseHorarioDTO::materia)
        .containsExactly("Lengua y Literatura", "Matemática");
    assertThat(dto.clases()).extracting(ClaseHorarioDTO::ahora).containsExactly(false, true);
    assertThat(dto.clases().get(0)).isEqualTo(new ClaseHorarioDTO(3L, 1L, "Lengua y Literatura",
        "#4F7BE8", "libro.svg", LocalTime.of(8, 45), LocalTime.of(9, 30), false));
  }

  // una materia del curso que no esta en la carpeta del alumno no aparece en su horario
  @Test
  void materiaQueNoCursa_noAparece() {
    MiHorarioDTO dto = servicioA("2026-10-19T10:30").obtenerMiHorario(mariel);

    assertThat(dto.clases()).extracting(ClaseHorarioDTO::materia).doesNotContain("Historia");
    assertThat(dto.clases()).extracting(ClaseHorarioDTO::ahora).containsOnly(false);
  }

  // justo a la hora de fin ya no esta en curso, a la de inicio si
  @Test
  void cambioDeHora_laClaseQueEmpiezaEsLaDeAhora() {
    MiHorarioDTO dto = servicioA("2026-10-19T09:30").obtenerMiHorario(mariel);

    assertThat(dto.clases()).extracting(ClaseHorarioDTO::ahora).containsExactly(false, true);
  }

  @Test
  void diaSinClases_devuelveMensajeYListaVacia() {
    // martes, sin horarios cargados
    MiHorarioDTO dto = servicioA("2026-10-20T09:00").obtenerMiHorario(mariel);

    assertThat(dto.dia()).isEqualTo(DiaSemana.MARTES);
    assertThat(dto.mensaje()).isEqualTo("Hoy no tienes clases");
    assertThat(dto.clases()).isEmpty();
  }

  @Test
  void finDeSemana_sinDiaNiClasesYNoConsultaHorarios() {
    // sabado
    MiHorarioDTO dto = servicioA("2026-10-17T09:00").obtenerMiHorario(mariel);

    assertThat(dto.dia()).isNull();
    assertThat(dto.mensaje()).isEqualTo("Hoy no tienes clases");
    assertThat(dto.clases()).isEmpty();
    verify(horarioRepository, never()).findByCursoIdAndDiaOrderByHoraInicioAsc(any(), any());
  }

  @Test
  void sinCarpetaDelCicloActual_esNoEncontrada() {
    Usuario sinCarpeta = Usuario.builder().id(99L).build();

    assertThatThrownBy(() -> servicioA("2026-10-19T09:00").obtenerMiHorario(sinCarpeta))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("No tienes una carpeta para el ciclo lectivo actual");
  }
}
