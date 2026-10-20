package edu.ies63.chispas2026_backend.services.impl;

import edu.ies63.chispas2026_backend.dtos.ActividadesMateriaDTO;
import edu.ies63.chispas2026_backend.dtos.MateriaCarpetaDTO;
import edu.ies63.chispas2026_backend.dtos.MiCarpetaDTO;
import edu.ies63.chispas2026_backend.enums.DiaSemana;
import edu.ies63.chispas2026_backend.enums.EstadoTarea;
import edu.ies63.chispas2026_backend.enums.EtiquetaMateria;
import edu.ies63.chispas2026_backend.exceptions.AccesoDenegadoException;
import edu.ies63.chispas2026_backend.models.*;
import edu.ies63.chispas2026_backend.repositories.CarpetaEspacioRepository;
import edu.ies63.chispas2026_backend.repositories.CarpetaRepository;
import edu.ies63.chispas2026_backend.repositories.HorarioRepository;
import edu.ies63.chispas2026_backend.repositories.TareaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CarpetaServiceImplTest {

  private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");

  private final CarpetaRepository carpetaRepository = mock(CarpetaRepository.class);
  private final CarpetaEspacioRepository carpetaEspacioRepository = mock(CarpetaEspacioRepository.class);
  private final HorarioRepository horarioRepository = mock(HorarioRepository.class);
  private final TareaRepository tareaRepository = mock(TareaRepository.class);

  private Usuario mariel;
  private Carpeta carpeta;
  private CarpetaEspacio lengua, matematica, historia;
  private EspacioCurricular espLengua, espMatematica, espHistoria;

  @BeforeEach
  void setUp() {
    mariel = Usuario.builder().id(2L).build();
    Estudiante estudiante = Estudiante.builder().id(2L).usuario(mariel).build();
    Curso curso = Curso.builder().id(1L).build();
    carpeta = Carpeta.builder().id(2L).estudiante(estudiante).curso(curso).nombre("Carpeta Rosa").build();

    espLengua = EspacioCurricular.builder().id(1L).nombre("Lengua y Literatura").build();
    espMatematica = EspacioCurricular.builder().id(2L).nombre("Matemática").build();
    espHistoria = EspacioCurricular.builder().id(3L).nombre("Historia").build();
    lengua = CarpetaEspacio.builder().id(3L).carpeta(carpeta).espacioCurricular(espLengua).orden(1).build();
    matematica = CarpetaEspacio.builder().id(4L).carpeta(carpeta).espacioCurricular(espMatematica).orden(2).build();
    historia = CarpetaEspacio.builder().id(5L).carpeta(carpeta).espacioCurricular(espHistoria).orden(3).build();

    when(carpetaRepository.findByEstudianteIdAndCicloLectivoActivoTrue(2L)).thenReturn(Optional.of(carpeta));
    when(carpetaEspacioRepository.findByCarpetaIdOrderByOrdenAsc(2L))
        .thenReturn(List.of(lengua, matematica, historia));
    // los lunes: lengua 08:45 a 09:30, mate 09:30 a 10:15, historia no tiene
    when(horarioRepository.findByCursoIdAndDiaOrderByHoraInicioAsc(1L, DiaSemana.LUNES)).thenReturn(List.of(
        horario(espLengua, "08:45", "09:30"),
        horario(espMatematica, "09:30", "10:15")
    ));
  }

  private Horario horario(EspacioCurricular espacio, String inicio, String fin) {
    return Horario.builder().espacioCurricular(espacio)
        .horaInicio(LocalTime.parse(inicio)).horaFin(LocalTime.parse(fin)).build();
  }

  private CarpetaServiceImpl servicioA(String fechaHora) {
    Clock clock = Clock.fixed(LocalDateTime.parse(fechaHora).atZone(ZONA).toInstant(), ZONA);
    return new CarpetaServiceImpl(carpetaRepository, carpetaEspacioRepository, horarioRepository, tareaRepository, clock);
  }

  @Test
  void enHorarioDeClase_materiaActualPrimeraConAhora_yLasDelDiaConHoy() {
    // lunes 9:40, esta en matematica
    MiCarpetaDTO dto = servicioA("2026-10-19T09:40").obtenerMiCarpeta(mariel);

    assertThat(dto.hayClaseAhora()).isTrue();
    assertThat(dto.mensaje()).isNull();
    assertThat(dto.materias()).extracting(MateriaCarpetaDTO::nombre)
        .containsExactly("Matemática", "Lengua y Literatura", "Historia");
    assertThat(dto.materias()).extracting(MateriaCarpetaDTO::etiqueta)
        .containsExactly(EtiquetaMateria.AHORA, EtiquetaMateria.HOY, null);
    assertThat(dto.materias().get(1).horaInicio()).isEqualTo(LocalTime.of(8, 45));
  }

  @Test
  void fueraDeHorario_muestraMensajeYNingunaMateriaConAhora() {
    // lunes 12hs, ya no hay clases
    MiCarpetaDTO dto = servicioA("2026-10-19T12:00").obtenerMiCarpeta(mariel);

    assertThat(dto.hayClaseAhora()).isFalse();
    assertThat(dto.mensaje()).isEqualTo("Ahora no tienes clases");
    assertThat(dto.materias()).hasSize(3);
    assertThat(dto.materias()).extracting(MateriaCarpetaDTO::etiqueta)
        .doesNotContain(EtiquetaMateria.AHORA);
  }

  @Test
  void finDeSemana_muestraMensajeYListaEnOrdenOriginal() {
    // sabado
    MiCarpetaDTO dto = servicioA("2026-10-17T09:00").obtenerMiCarpeta(mariel);

    assertThat(dto.mensaje()).isEqualTo("Ahora no tienes clases");
    assertThat(dto.materias()).extracting(MateriaCarpetaDTO::nombre)
        .containsExactly("Lengua y Literatura", "Matemática", "Historia");
    assertThat(dto.materias()).extracting(MateriaCarpetaDTO::etiqueta).containsOnlyNulls();
  }

  @Test
  void materiaConActividades_muestraTituloFechaYEstado() {
    Tarea finalizada = Tarea.builder().id(1L).fechaAsignacion(LocalDate.of(2026, 10, 16))
        .estado(EstadoTarea.FINALIZADA).actividad(Actividad.builder().id(1L).titulo("Ordenar la historia").build()).build();
    Tarea pendiente = Tarea.builder().id(2L).fechaAsignacion(LocalDate.of(2026, 10, 16))
        .estado(EstadoTarea.EN_CURSO).actividad(Actividad.builder().id(3L).titulo("Unir con flechas").build()).build();
    when(carpetaEspacioRepository.findConEspacioYCarpetaById(3L)).thenReturn(Optional.of(lengua));
    when(tareaRepository.findByCarpetaEspacioIdOrderByFechaAsignacionDescIdDesc(3L))
        .thenReturn(List.of(pendiente, finalizada));

    ActividadesMateriaDTO dto = servicioA("2026-10-19T09:40").obtenerActividadesDeMateria(mariel, 3L);

    assertThat(dto.mensaje()).isNull();
    assertThat(dto.actividades()).hasSize(2);
    assertThat(dto.actividades().get(0).estado()).isEqualTo("Pendiente");
    assertThat(dto.actividades().get(1).estado()).isEqualTo("Completada");
    assertThat(dto.actividades().get(1).titulo()).isEqualTo("Ordenar la historia");
  }

  @Test
  void materiaSinActividades_muestraMensaje() {
    when(carpetaEspacioRepository.findConEspacioYCarpetaById(5L)).thenReturn(Optional.of(historia));
    when(tareaRepository.findByCarpetaEspacioIdOrderByFechaAsignacionDescIdDesc(5L)).thenReturn(List.of());

    ActividadesMateriaDTO dto = servicioA("2026-10-19T09:40").obtenerActividadesDeMateria(mariel, 5L);

    assertThat(dto.actividades()).isEmpty();
    assertThat(dto.mensaje()).isEqualTo("Todavía no hay actividades en esta materia");
  }

  @Test
  void carpetaDeOtroAlumno_deniegaAcceso() {
    Usuario tomas = Usuario.builder().id(3L).build();
    when(carpetaEspacioRepository.findConEspacioYCarpetaById(3L)).thenReturn(Optional.of(lengua));

    assertThatThrownBy(() -> servicioA("2026-10-19T09:40").obtenerActividadesDeMateria(tomas, 3L))
        .isInstanceOf(AccesoDenegadoException.class)
        .hasMessage("No tienes permiso para ver esta carpeta");
    verifyNoInteractions(tareaRepository);
  }
}
