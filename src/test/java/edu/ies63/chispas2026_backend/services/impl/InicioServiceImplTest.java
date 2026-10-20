package edu.ies63.chispas2026_backend.services.impl;

import edu.ies63.chispas2026_backend.dtos.ActividadCarpetaDTO;
import edu.ies63.chispas2026_backend.dtos.InicioDTO;
import edu.ies63.chispas2026_backend.dtos.MateriaCarpetaDTO;
import edu.ies63.chispas2026_backend.dtos.MateriaInicioDTO;
import edu.ies63.chispas2026_backend.dtos.MiCarpetaDTO;
import edu.ies63.chispas2026_backend.enums.CodigoPlantilla;
import edu.ies63.chispas2026_backend.enums.EstadoTarea;
import edu.ies63.chispas2026_backend.enums.EtiquetaMateria;
import edu.ies63.chispas2026_backend.models.*;
import edu.ies63.chispas2026_backend.repositories.TareaRepository;
import edu.ies63.chispas2026_backend.services.CarpetaService;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

// TSDS-6: tests unitarios del Inicio. La carpeta (materias ordenadas y etiquetadas) sale del
// CarpetaService mockeado; aca se prueba como se reparten las pendientes en cada materia
class InicioServiceImplTest {

  private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
  private static final long CARPETA_MARIEL = 2L;

  private final CarpetaService carpetaService = mock(CarpetaService.class);
  private final TareaRepository tareaRepository = mock(TareaRepository.class);
  private final Usuario mariel = Usuario.builder().id(2L).build();

  // materias como las devuelve la carpeta: Matematica en clase, Lengua hoy y Historia otro dia
  private final MateriaCarpetaDTO matematica = new MateriaCarpetaDTO(4L, 2L, "Matemática", "#F00", "calc",
      EtiquetaMateria.AHORA, LocalTime.parse("10:00"), LocalTime.parse("12:00"));
  private final MateriaCarpetaDTO lengua = new MateriaCarpetaDTO(3L, 1L, "Lengua y Literatura", "#00F", "book",
      EtiquetaMateria.HOY, LocalTime.parse("08:00"), LocalTime.parse("10:00"));
  private final MateriaCarpetaDTO historia = new MateriaCarpetaDTO(5L, 3L, "Historia", "#0F0", "globe",
      null, null, null);

  private InicioServiceImpl servicioA(String fechaHora) {
    Clock clock = Clock.fixed(LocalDateTime.parse(fechaHora).atZone(ZONA).toInstant(), ZONA);
    return new InicioServiceImpl(carpetaService, tareaRepository, clock);
  }

  private void carpetaConClase(boolean hayClaseAhora, MateriaCarpetaDTO... materias) {
    when(carpetaService.obtenerMiCarpeta(mariel)).thenReturn(new MiCarpetaDTO(CARPETA_MARIEL, "Carpeta Rosa",
        "#FFC0CB", hayClaseAhora, hayClaseAhora ? null : "Ahora no tienes clases", List.of(materias)));
  }

  // tarea pendiente de la materia (carpetaEspacio) indicada
  private Tarea tarea(long id, long carpetaEspacioId, String fecha, EstadoTarea estado) {
    Plantilla plantilla = Plantilla.builder().codigo(CodigoPlantilla.ORDENAR).nombre("Ordenar la historia").build();
    Contenido contenido = Contenido.builder().portadaUrl("https://img/portada-" + id + ".jpg").build();
    Actividad actividad = Actividad.builder().id(id * 10).titulo("Actividad " + id)
        .plantilla(plantilla).contenido(contenido).build();
    return Tarea.builder().id(id).actividad(actividad)
        .carpetaEspacio(CarpetaEspacio.builder().id(carpetaEspacioId).build())
        .fechaAsignacion(LocalDate.parse(fecha)).estado(estado).build();
  }

  private List<Long> idsDe(List<ActividadCarpetaDTO> actividades) {
    return actividades.stream().map(ActividadCarpetaDTO::tareaId).toList();
  }

  // escenario 4: en horario de Matematica, su pendiente mas reciente es la principal
  @Test
  void enHorarioDeClase_laMateriaActualVaPrimeraConSuActividadPrincipal() {
    carpetaConClase(true, matematica, lengua, historia);
    when(tareaRepository.findPendientesHastaHoy(CARPETA_MARIEL, LocalDate.parse("2026-10-20"))).thenReturn(List.of(
        tarea(7L, 4L, "2026-10-20", EstadoTarea.PENDIENTE),
        tarea(6L, 3L, "2026-10-19", EstadoTarea.PENDIENTE),
        tarea(5L, 4L, "2026-10-15", EstadoTarea.EN_CURSO),
        tarea(4L, 4L, "2026-10-10", EstadoTarea.PENDIENTE)
    ));

    InicioDTO dto = servicioA("2026-10-20T11:00").obtenerInicio(mariel);

    assertThat(dto.hayClaseAhora()).isTrue();
    assertThat(dto.mensaje()).isNull();
    assertThat(dto.materias()).extracting(MateriaInicioDTO::nombre)
        .containsExactly("Matemática", "Lengua y Literatura", "Historia");

    MateriaInicioDTO mate = dto.materias().get(0);
    assertThat(mate.actividadPrincipal().tareaId()).isEqualTo(7L);
    assertThat(mate.actividadPrincipal().esHoy()).isTrue();
    assertThat(idsDe(mate.otrasPendientes())).containsExactly(5L, 4L);
    assertThat(mate.mensaje()).isNull();

    MateriaInicioDTO lenguaDto = dto.materias().get(1);
    assertThat(lenguaDto.actividadPrincipal().tareaId()).isEqualTo(6L);
    assertThat(lenguaDto.actividadPrincipal().esHoy()).isFalse();
    assertThat(lenguaDto.otrasPendientes()).isEmpty();
  }

  // punto 4: materia sin pendientes, principal en null, lista vacia y mensaje
  @Test
  void materiaSinPendientes_principalNullYMensaje() {
    carpetaConClase(true, matematica, historia);
    when(tareaRepository.findPendientesHastaHoy(eq(CARPETA_MARIEL), any()))
        .thenReturn(List.of(tarea(7L, 4L, "2026-10-20", EstadoTarea.PENDIENTE)));

    MateriaInicioDTO historiaDto = servicioA("2026-10-20T11:00").obtenerInicio(mariel).materias().get(1);

    assertThat(historiaDto.actividadPrincipal()).isNull();
    assertThat(historiaDto.otrasPendientes()).isEmpty();
    assertThat(historiaDto.mensaje()).isEqualTo(InicioServiceImpl.MSG_SIN_PENDIENTES);
  }

  // desde la casa (fuera de horario): mensaje de sin clases, pero igual vienen todas las materias con sus pendientes
  @Test
  void fueraDeHorario_mandaElMensajePeroIgualMuestraLasPendientes() {
    carpetaConClase(false, lengua, matematica, historia);
    when(tareaRepository.findPendientesHastaHoy(eq(CARPETA_MARIEL), any()))
        .thenReturn(List.of(tarea(6L, 3L, "2026-10-19", EstadoTarea.PENDIENTE)));

    InicioDTO dto = servicioA("2026-10-20T19:00").obtenerInicio(mariel);

    assertThat(dto.hayClaseAhora()).isFalse();
    assertThat(dto.mensaje()).isEqualTo("Ahora no tienes clases");
    assertThat(dto.materias()).hasSize(3);
    assertThat(dto.materias().get(0).actividadPrincipal().tareaId()).isEqualTo(6L);
  }

  // copia los datos de la materia tal cual vienen de la carpeta (etiqueta, horario, color, icono)
  @Test
  void copiaLosDatosDeLaMateria() {
    carpetaConClase(true, matematica);
    when(tareaRepository.findPendientesHastaHoy(eq(CARPETA_MARIEL), any())).thenReturn(List.of());

    MateriaInicioDTO mate = servicioA("2026-10-20T11:00").obtenerInicio(mariel).materias().get(0);

    assertThat(mate.carpetaEspacioId()).isEqualTo(4L);
    assertThat(mate.espacioCurricularId()).isEqualTo(2L);
    assertThat(mate.color()).isEqualTo("#F00");
    assertThat(mate.icono()).isEqualTo("calc");
    assertThat(mate.etiqueta()).isEqualTo(EtiquetaMateria.AHORA);
    assertThat(mate.horaInicio()).isEqualTo("10:00");
    assertThat(mate.horaFin()).isEqualTo("12:00");
  }

  // "hoy" es el dia de Argentina: a las 23:30 del 20 (02:30 UTC del 21) consulta con el 20
  @Test
  void consultaLasPendientesConElDiaDeArgentina() {
    carpetaConClase(false);
    when(tareaRepository.findPendientesHastaHoy(eq(CARPETA_MARIEL), any())).thenReturn(List.of());

    servicioA("2026-10-20T23:30").obtenerInicio(mariel);

    verify(tareaRepository).findPendientesHastaHoy(CARPETA_MARIEL, LocalDate.parse("2026-10-20"));
  }
}
