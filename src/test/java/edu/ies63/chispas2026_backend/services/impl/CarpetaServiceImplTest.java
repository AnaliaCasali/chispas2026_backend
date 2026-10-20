package edu.ies63.chispas2026_backend.services.impl;

import edu.ies63.chispas2026_backend.dtos.ActividadCarpetaDTO;
import edu.ies63.chispas2026_backend.dtos.ActividadesMateriaDTO;
import edu.ies63.chispas2026_backend.dtos.MateriaCarpetaDTO;
import edu.ies63.chispas2026_backend.dtos.MiCarpetaDTO;
import edu.ies63.chispas2026_backend.dtos.TipoActividadDTO;
import edu.ies63.chispas2026_backend.enums.CodigoPlantilla;
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

  // TSDS-4: arma una tarea con su actividad, plantilla y contenido (lo que usa el DTO)
  private Tarea tarea(long id, String titulo, CodigoPlantilla codigo, String fecha, EstadoTarea estado) {
    Plantilla plantilla = Plantilla.builder().codigo(codigo)
        .nombre(codigo == CodigoPlantilla.ORDENAR ? "Ordenar la historia" : "Relación con imágenes").build();
    Contenido contenido = Contenido.builder().portadaUrl("https://img/portada-" + id + ".jpg").build();
    Actividad actividad = Actividad.builder().id(id * 10).titulo(titulo)
        .plantilla(plantilla).contenido(contenido).build();
    return Tarea.builder().id(id).actividad(actividad)
        .fechaAsignacion(LocalDate.parse(fecha)).estado(estado).build();
  }

  // TSDS-4: editado. "Completada" pasa a "Realizada", se usa la nueva consulta del repo
  @Test
  void materiaConActividades_muestraTituloFechaYEstado() {
    Tarea finalizada = tarea(1L, "Ordenar la historia", CodigoPlantilla.ORDENAR, "2026-10-16", EstadoTarea.FINALIZADA);
    Tarea pendiente = tarea(2L, "Unir con flechas", CodigoPlantilla.UNIR_FLECHAS, "2026-10-16", EstadoTarea.EN_CURSO);
    when(carpetaEspacioRepository.findConEspacioYCarpetaById(3L)).thenReturn(Optional.of(lengua));
    when(tareaRepository.findAsignadasHastaHoy(3L, LocalDate.of(2026, 10, 19)))
        .thenReturn(List.of(pendiente, finalizada));

    ActividadesMateriaDTO dto = servicioA("2026-10-19T09:40").obtenerActividadesDeMateria(mariel, 3L);

    assertThat(dto.mensaje()).isNull();
    assertThat(dto.actividades()).hasSize(2);
    assertThat(dto.actividades().get(0).estado()).isEqualTo("Pendiente");
    assertThat(dto.actividades().get(1).estado()).isEqualTo("Realizada");
    assertThat(dto.actividades().get(1).titulo()).isEqualTo("Ordenar la historia");
  }

  // TSDS-4: el service pide al repo solo hasta la fecha de hoy en Argentina
  @Test
  void consultaSoloTareasAsignadasHastaHoy() {
    when(carpetaEspacioRepository.findConEspacioYCarpetaById(3L)).thenReturn(Optional.of(lengua));
    when(tareaRepository.findAsignadasHastaHoy(anyLong(), any())).thenReturn(List.of());

    // 23:30 del 19 en Argentina ya es 20 en UTC: hoy tiene que ser 19
    servicioA("2026-10-19T23:30").obtenerActividadesDeMateria(mariel, 3L);

    verify(tareaRepository).findAsignadasHastaHoy(3L, LocalDate.of(2026, 10, 19));
  }

  // TSDS-4: respeta el orden que devuelve el repo (fecha y hora desc) sin reordenar
  @Test
  void mantieneElOrdenDelRepositorio() {
    Tarea nueva = tarea(5L, "Mi mañana", CodigoPlantilla.ORDENAR, "2026-10-19", EstadoTarea.PENDIENTE);
    Tarea mismoDiaMasTemprano = tarea(9L, "Unir con flechas", CodigoPlantilla.UNIR_FLECHAS, "2026-10-19", EstadoTarea.PENDIENTE);
    Tarea vieja = tarea(1L, "Ordenar la historia", CodigoPlantilla.ORDENAR, "2026-10-08", EstadoTarea.FINALIZADA);
    when(carpetaEspacioRepository.findConEspacioYCarpetaById(3L)).thenReturn(Optional.of(lengua));
    when(tareaRepository.findAsignadasHastaHoy(3L, LocalDate.of(2026, 10, 19)))
        .thenReturn(List.of(nueva, mismoDiaMasTemprano, vieja));

    ActividadesMateriaDTO dto = servicioA("2026-10-19T09:40").obtenerActividadesDeMateria(mariel, 3L);

    assertThat(dto.actividades()).extracting(ActividadCarpetaDTO::tareaId).containsExactly(5L, 9L, 1L);
  }

  // TSDS-4: esHoy solo para las asignadas hoy (etiqueta HOY en el front)
  @Test
  void marcaEsHoySoloEnLasAsignadasHoy() {
    Tarea deHoy = tarea(2L, "Unir con flechas", CodigoPlantilla.UNIR_FLECHAS, "2026-10-19", EstadoTarea.PENDIENTE);
    Tarea deAyer = tarea(1L, "Ordenar la historia", CodigoPlantilla.ORDENAR, "2026-10-18", EstadoTarea.PENDIENTE);
    when(carpetaEspacioRepository.findConEspacioYCarpetaById(3L)).thenReturn(Optional.of(lengua));
    when(tareaRepository.findAsignadasHastaHoy(3L, LocalDate.of(2026, 10, 19))).thenReturn(List.of(deHoy, deAyer));

    ActividadesMateriaDTO dto = servicioA("2026-10-19T09:40").obtenerActividadesDeMateria(mariel, 3L);

    assertThat(dto.actividades()).extracting(ActividadCarpetaDTO::esHoy).containsExactly(true, false);
  }

  // TSDS-4: solo FINALIZADA es realizada; PENDIENTE y EN_CURSO se muestran como pendientes
  @Test
  void realizadaSoloSiEstaFinalizada() {
    when(carpetaEspacioRepository.findConEspacioYCarpetaById(3L)).thenReturn(Optional.of(lengua));
    when(tareaRepository.findAsignadasHastaHoy(3L, LocalDate.of(2026, 10, 19))).thenReturn(List.of(
        tarea(3L, "A", CodigoPlantilla.ORDENAR, "2026-10-16", EstadoTarea.FINALIZADA),
        tarea(2L, "B", CodigoPlantilla.ORDENAR, "2026-10-16", EstadoTarea.EN_CURSO),
        tarea(1L, "C", CodigoPlantilla.ORDENAR, "2026-10-16", EstadoTarea.PENDIENTE)
    ));

    ActividadesMateriaDTO dto = servicioA("2026-10-19T09:40").obtenerActividadesDeMateria(mariel, 3L);

    assertThat(dto.actividades()).extracting(ActividadCarpetaDTO::realizada).containsExactly(true, false, false);
    assertThat(dto.actividades()).extracting(ActividadCarpetaDTO::estado)
        .containsExactly("Realizada", "Pendiente", "Pendiente");
  }

  // TSDS-4: cada tarea trae tipo de actividad e imagen (portada del contenido)
  @Test
  void incluyeTipoDeActividadEImagen() {
    when(carpetaEspacioRepository.findConEspacioYCarpetaById(3L)).thenReturn(Optional.of(lengua));
    when(tareaRepository.findAsignadasHastaHoy(3L, LocalDate.of(2026, 10, 19))).thenReturn(List.of(
        tarea(2L, "Unir con flechas", CodigoPlantilla.UNIR_FLECHAS, "2026-10-16", EstadoTarea.PENDIENTE)));

    ActividadCarpetaDTO actividad = servicioA("2026-10-19T09:40")
        .obtenerActividadesDeMateria(mariel, 3L).actividades().get(0);

    assertThat(actividad.tipoActividad())
        .isEqualTo(new TipoActividadDTO(CodigoPlantilla.UNIR_FLECHAS, "Relación con imágenes"));
    assertThat(actividad.imagenUrl()).isEqualTo("https://img/portada-2.jpg");
    assertThat(actividad.actividadId()).isEqualTo(20L);
  }

  // TSDS-4: editado, usa la nueva consulta del repo
  @Test
  void materiaSinActividades_muestraMensaje() {
    when(carpetaEspacioRepository.findConEspacioYCarpetaById(5L)).thenReturn(Optional.of(historia));
    when(tareaRepository.findAsignadasHastaHoy(5L, LocalDate.of(2026, 10, 19))).thenReturn(List.of());

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
