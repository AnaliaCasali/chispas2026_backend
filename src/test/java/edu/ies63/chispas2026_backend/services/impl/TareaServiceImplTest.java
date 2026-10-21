package edu.ies63.chispas2026_backend.services.impl;

import edu.ies63.chispas2026_backend.dtos.EntregaRequestDTO;
import edu.ies63.chispas2026_backend.dtos.ItemRespuestaDTO;
import edu.ies63.chispas2026_backend.dtos.ResultadoTareaDTO;
import edu.ies63.chispas2026_backend.enums.CodigoPlantilla;
import edu.ies63.chispas2026_backend.enums.EstadoTarea;
import edu.ies63.chispas2026_backend.enums.LadoItem;
import edu.ies63.chispas2026_backend.exceptions.AccesoDenegadoException;
import edu.ies63.chispas2026_backend.exceptions.RespuestaInvalidaException;
import edu.ies63.chispas2026_backend.exceptions.TareaYaFinalizadaException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import edu.ies63.chispas2026_backend.models.*;
import edu.ies63.chispas2026_backend.repositories.ActividadImagenRepository;
import edu.ies63.chispas2026_backend.repositories.ActividadParRepository;
import edu.ies63.chispas2026_backend.repositories.RespuestaRepository;
import edu.ies63.chispas2026_backend.repositories.TareaRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

// TSDS-13: corrige por aciertos, guarda el intento sin borrar nada y elige el mensaje del resultado
class TareaServiceImplTest {

  private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
  private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 21, 10, 0);

  private final TareaRepository tareaRepository = mock(TareaRepository.class);
  private final RespuestaRepository respuestaRepository = mock(RespuestaRepository.class);
  private final ActividadImagenRepository actividadImagenRepository = mock(ActividadImagenRepository.class);
  private final ActividadParRepository actividadParRepository = mock(ActividadParRepository.class);
  private final Clock clock = Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA);

  private TareaServiceImpl servicio;

  private Usuario mariel;
  private Tarea tareaOrdenar;  // ORDENAR: 4 items, la posicion correcta es su propio nroOrden
  private Tarea tareaUnir;     // UNIR_FLECHAS: 4 izq unidos a 4 der

  @BeforeEach
  void setUp() {
    servicio = new TareaServiceImpl(tareaRepository, respuestaRepository,
        actividadImagenRepository, actividadParRepository, clock);

    mariel = Usuario.builder().id(2L).build();
    Estudiante estudiante = Estudiante.builder().id(2L).usuario(mariel).build();
    Carpeta carpeta = Carpeta.builder().id(2L).estudiante(estudiante)
        .curso(Curso.builder().id(1L).build()).nombre("Carpeta Rosa").build();
    CarpetaEspacio lengua = CarpetaEspacio.builder().id(3L).carpeta(carpeta)
        .espacioCurricular(EspacioCurricular.builder().id(1L).nombre("Lengua y Literatura").build())
        .orden(1).build();

    tareaOrdenar = tarea(9L, lengua, 20L, plantilla(CodigoPlantilla.ORDENAR));
    tareaUnir = tarea(10L, lengua, 21L, plantilla(CodigoPlantilla.UNIR_FLECHAS));
  }

  // ---------- helpers ----------

  private Plantilla plantilla(CodigoPlantilla codigo) {
    return Plantilla.builder().id(codigo == CodigoPlantilla.ORDENAR ? 1L : 2L)
        .codigo(codigo)
        .nombre(codigo == CodigoPlantilla.ORDENAR ? "Ordenar la historia" : "Relación con imágenes")
        .minItems(1).maxItems(10).build();
  }

  private Tarea tarea(long id, CarpetaEspacio lengua, long actividadId, Plantilla plantilla) {
    Actividad actividad = Actividad.builder().id(actividadId)
        .contenido(Contenido.builder().id(1L).tema("Tema").texto("Texto").build())
        .plantilla(plantilla).creadaPor(mariel).titulo("Una actividad").consigna("Hacela").build();
    return Tarea.builder().id(id).actividad(actividad).carpetaEspacio(lengua)
        .asignadaPor(mariel).fechaAsignacion(LocalDate.of(2026, 10, 19)).build();
  }

  private ActividadImagen item(long id, int nroOrden, LadoItem lado) {
    return ActividadImagen.builder().id(id).nroOrden(nroOrden).lado(lado)
        .imagen(Imagen.builder().id(id).build()).titulo("Ítem " + id).build();
  }

  private ItemRespuestaDTO orden(Long itemId, int posicion) {
    return new ItemRespuestaDTO(itemId, posicion, null);
  }

  private ItemRespuestaDTO union(Long itemId, Long unidoId) {
    return new ItemRespuestaDTO(itemId, null, unidoId);
  }

  private EntregaRequestDTO entrega(ItemRespuestaDTO... items) {
    return new EntregaRequestDTO(List.of(items));
  }

  // la tarea 9 es ORDENAR con 4 items unicos, la correcta es su propio nroOrden
  private void dadoOrdenar() {
    when(tareaRepository.findCompletaById(9L)).thenReturn(Optional.of(tareaOrdenar));
    when(actividadImagenRepository.findByActividadId(20L)).thenReturn(List.of(
        item(101L, 1, LadoItem.UNICA), item(102L, 2, LadoItem.UNICA),
        item(103L, 3, LadoItem.UNICA), item(104L, 4, LadoItem.UNICA)));
    when(respuestaRepository.maxIntentoPorTarea(9L)).thenReturn(0);
  }

  // la tarea 10 es UNIR_FLECHAS: izqN se une con derN (ids 20x a la izquierda y 30x a la derecha)
  private void dadoUnir() {
    List<ActividadImagen> items = List.of(
        item(201L, 1, LadoItem.IZQ), item(202L, 2, LadoItem.IZQ),
        item(203L, 3, LadoItem.IZQ), item(204L, 4, LadoItem.IZQ),
        item(301L, 1, LadoItem.DER), item(302L, 2, LadoItem.DER),
        item(303L, 3, LadoItem.DER), item(304L, 4, LadoItem.DER));
    when(tareaRepository.findCompletaById(10L)).thenReturn(Optional.of(tareaUnir));
    when(actividadImagenRepository.findByActividadId(21L)).thenReturn(items);
    when(actividadParRepository.findConItemsByActividadId(21L)).thenReturn(List.of(
        ActividadPar.builder().itemIzq(items.get(0)).itemDer(items.get(4)).build(),
        ActividadPar.builder().itemIzq(items.get(1)).itemDer(items.get(5)).build(),
        ActividadPar.builder().itemIzq(items.get(2)).itemDer(items.get(6)).build(),
        ActividadPar.builder().itemIzq(items.get(3)).itemDer(items.get(7)).build()));
    when(respuestaRepository.maxIntentoPorTarea(10L)).thenReturn(0);
  }

  // captura las respuestas que el service guardo con saveAll
  @SuppressWarnings("unchecked")
  private List<Respuesta> respuestasGuardadas() {
    ArgumentCaptor<List<Respuesta>> captor = ArgumentCaptor.forClass(List.class);
    verify(respuestaRepository).saveAll(captor.capture());
    return captor.getValue();
  }

  // ---------- ORDENAR ----------

  // 4/4: felicitaciones, FINALIZADA y las 4 respuestas del intento 1 todas correctas
  @Test
  void ordenarTodoCorrecto_felicitacionesYFinaliza() {
    dadoOrdenar();

    ResultadoTareaDTO resultado = servicio.entregar(mariel, 9L, entrega(
        orden(101L, 1), orden(102L, 2), orden(103L, 3), orden(104L, 4)));

    assertThat(resultado.mensaje()).isEqualTo(TareaServiceImpl.MSG_TODO_BIEN);
    assertThat(resultado.aciertos()).isEqualTo(4);
    assertThat(resultado.totalItems()).isEqualTo(4);
    assertThat(resultado.estado()).isEqualTo("Realizada");
    assertThat(resultado.puedeIntentar()).isFalse();

    assertThat(tareaOrdenar.getEstado()).isEqualTo(EstadoTarea.FINALIZADA);
    assertThat(tareaOrdenar.getIniciadaEn()).isEqualTo(AHORA);
    assertThat(tareaOrdenar.getFinalizadaEn()).isEqualTo(AHORA);
    assertThat(tareaOrdenar.getDuracionMinutos()).isZero();
    assertThat(tareaOrdenar.getAciertos()).isEqualTo(4);
    assertThat(tareaOrdenar.getTotalItems()).isEqualTo(4);
    verify(tareaRepository).save(tareaOrdenar);

    assertThat(respuestasGuardadas()).hasSize(4)
        .allSatisfy(r -> assertThat(r.isEsCorrecta()).isTrue())
        .allSatisfy(r -> assertThat(r.getId().getIntento()).isEqualTo(1));
  }

  // 3/4: muy cerca, queda EN_CURSO y puede reintentar
  @Test
  void ordenarTresDeCuatro_muyCerca() {
    dadoOrdenar();

    ResultadoTareaDTO resultado = servicio.entregar(mariel, 9L, entrega(
        orden(101L, 1), orden(102L, 2), orden(103L, 3), orden(104L, 3)));

    assertThat(resultado.aciertos()).isEqualTo(3);
    assertThat(resultado.mensaje()).isEqualTo(TareaServiceImpl.MSG_MUY_CERCA);
    assertThat(resultado.estado()).isEqualTo("Pendiente");
    assertThat(resultado.puedeIntentar()).isTrue();
    assertThat(tareaOrdenar.getEstado()).isEqualTo(EstadoTarea.EN_CURSO);
    assertThat(tareaOrdenar.getFinalizadaEn()).isNull();
  }

  // 2/4: el umbral justo (la mitad de 4) sigue siendo muy cerca
  @Test
  void ordenarDosDeCuatro_muyCerca() {
    dadoOrdenar();

    ResultadoTareaDTO resultado = servicio.entregar(mariel, 9L, entrega(
        orden(101L, 1), orden(102L, 1), orden(103L, 4), orden(104L, 4)));

    assertThat(resultado.aciertos()).isEqualTo(2);
    assertThat(resultado.mensaje()).isEqualTo(TareaServiceImpl.MSG_MUY_CERCA);
  }

  // 1/4: buen intento, puede reintentar
  @Test
  void ordenarUnoDeCuatro_buenIntento() {
    dadoOrdenar();

    ResultadoTareaDTO resultado = servicio.entregar(mariel, 9L, entrega(
        orden(101L, 1), orden(102L, 3), orden(103L, 4), orden(104L, 2)));

    assertThat(resultado.aciertos()).isEqualTo(1);
    assertThat(resultado.mensaje()).isEqualTo(TareaServiceImpl.MSG_BUEN_INTENTO);
    assertThat(resultado.puedeIntentar()).isTrue();
  }

  // 0/4: buen intento
  @Test
  void ordenarNinguno_buenIntento() {
    dadoOrdenar();

    ResultadoTareaDTO resultado = servicio.entregar(mariel, 9L, entrega(
        orden(101L, 2), orden(102L, 1), orden(103L, 4), orden(104L, 3)));

    assertThat(resultado.aciertos()).isZero();
    assertThat(resultado.mensaje()).isEqualTo(TareaServiceImpl.MSG_BUEN_INTENTO);
  }

  // ---------- UNIR_FLECHAS ----------

  @Test
  void unirTodoCorrecto_felicitaciones() {
    dadoUnir();

    ResultadoTareaDTO resultado = servicio.entregar(mariel, 10L, entrega(
        union(201L, 301L), union(202L, 302L), union(203L, 303L), union(204L, 304L)));

    assertThat(resultado.aciertos()).isEqualTo(4);
    assertThat(resultado.mensaje()).isEqualTo(TareaServiceImpl.MSG_TODO_BIEN);
    assertThat(tareaUnir.getEstado()).isEqualTo(EstadoTarea.FINALIZADA);
  }

  // cuenta solo los pares correctos: 202 y 203 cruzados
  @Test
  void unirConCruzados_cuentaSoloLosParesCorrectos() {
    dadoUnir();

    ResultadoTareaDTO resultado = servicio.entregar(mariel, 10L, entrega(
        union(201L, 301L), union(202L, 303L), union(203L, 302L), union(204L, 304L)));

    assertThat(resultado.aciertos()).isEqualTo(2);
    assertThat(resultado.totalItems()).isEqualTo(4);
    assertThat(resultado.mensaje()).isEqualTo(TareaServiceImpl.MSG_MUY_CERCA);
  }

  // ---------- reintentos ----------

  // el reintento guarda con intento 2 y no pisa el iniciadaEn del primero
  @Test
  void reintento_guardaElIntentoDosYNoPisaIniciadaEn() {
    dadoOrdenar();
    LocalDateTime inicioOriginal = LocalDateTime.of(2026, 10, 21, 9, 0);
    tareaOrdenar.setIniciadaEn(inicioOriginal);
    tareaOrdenar.setEstado(EstadoTarea.EN_CURSO);
    when(respuestaRepository.maxIntentoPorTarea(9L)).thenReturn(1);

    ResultadoTareaDTO resultado = servicio.entregar(mariel, 9L, entrega(
        orden(101L, 1), orden(102L, 2), orden(103L, 3), orden(104L, 3)));

    assertThat(resultado.aciertos()).isEqualTo(3);
    assertThat(tareaOrdenar.getIniciadaEn()).isEqualTo(inicioOriginal);
    assertThat(tareaOrdenar.getEstado()).isEqualTo(EstadoTarea.EN_CURSO);
    assertThat(respuestasGuardadas())
        .allSatisfy(r -> assertThat(r.getId().getIntento()).isEqualTo(2));
  }

  // ---------- seguridad y estado ----------

  @Test
  void tareaDeOtroAlumno_accesoDenegado() {
    dadoOrdenar();
    Usuario tomas = Usuario.builder().id(3L).build();

    assertThatThrownBy(() -> servicio.entregar(tomas, 9L, entrega(orden(101L, 1))))
        .isInstanceOf(AccesoDenegadoException.class)
        .hasMessage(TareaServiceImpl.MSG_SIN_PERMISO);
    verifyNoInteractions(respuestaRepository, actividadImagenRepository, actividadParRepository);
  }

  @Test
  void tareaYaFinalizada_noSePisaElResultado() {
    dadoOrdenar();
    tareaOrdenar.setEstado(EstadoTarea.FINALIZADA);

    assertThatThrownBy(() -> servicio.entregar(mariel, 9L, entrega(orden(101L, 1))))
        .isInstanceOf(TareaYaFinalizadaException.class)
        .hasMessage(TareaServiceImpl.MSG_YA_FINALIZADA);
    verifyNoInteractions(respuestaRepository, actividadImagenRepository, actividadParRepository);
  }

  @Test
  void tareaInexistente_lanzaNoEncontrada() {
    when(tareaRepository.findCompletaById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> servicio.entregar(mariel, 999L, entrega(orden(101L, 1))))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("La tarea solicitada no existe");
    verifyNoInteractions(respuestaRepository, actividadImagenRepository, actividadParRepository);
  }

  // ---------- entregas invalidas ----------

  @Test
  void entregaConMenosItemsQueLaActividad_esInvalida() {
    dadoOrdenar();

    assertThatThrownBy(() -> servicio.entregar(mariel, 9L, entrega(
        orden(101L, 1), orden(102L, 2))))
        .isInstanceOf(RespuestaInvalidaException.class)
        .hasMessage("Debes responder todos los ítems de la actividad");
    verify(respuestaRepository, never()).saveAll(any());
  }

  @Test
  void entregaConItemRepetido_esInvalida() {
    dadoOrdenar();

    assertThatThrownBy(() -> servicio.entregar(mariel, 9L, entrega(
        orden(101L, 1), orden(101L, 2), orden(103L, 3), orden(104L, 4))))
        .isInstanceOf(RespuestaInvalidaException.class)
        .hasMessage("Cada ítem de la actividad se responde una sola vez");
    verify(respuestaRepository, never()).saveAll(any());
  }

  @Test
  void entregaConItemDeOtraActividad_esInvalida() {
    dadoOrdenar();

    assertThatThrownBy(() -> servicio.entregar(mariel, 9L, entrega(
        orden(999L, 1), orden(102L, 2), orden(103L, 3), orden(104L, 4))))
        .isInstanceOf(RespuestaInvalidaException.class)
        .hasMessage("Cada ítem de la actividad se responde una sola vez");
    verify(respuestaRepository, never()).saveAll(any());
  }

  @Test
  void ordenarSinPosicionElegida_esInvalida() {
    dadoOrdenar();

    assertThatThrownBy(() -> servicio.entregar(mariel, 9L, entrega(
        new ItemRespuestaDTO(101L, null, null), orden(102L, 2), orden(103L, 3), orden(104L, 4))))
        .isInstanceOf(RespuestaInvalidaException.class)
        .hasMessage("Cada ítem debe tener la posición elegida");
    verify(respuestaRepository, never()).saveAll(any());
  }

  @Test
  void unirSinItemUnido_esInvalida() {
    dadoUnir();

    assertThatThrownBy(() -> servicio.entregar(mariel, 10L, entrega(
        new ItemRespuestaDTO(201L, null, null), union(202L, 302L),
        union(203L, 303L), union(204L, 304L))))
        .isInstanceOf(RespuestaInvalidaException.class)
        .hasMessage("Cada ítem debe unirse con uno de la columna derecha");
    verify(respuestaRepository, never()).saveAll(any());
  }

  // une un izq con otro izq: el unido no es de la columna derecha
  @Test
  void unirConUnItemIzquierdo_esInvalida() {
    dadoUnir();

    assertThatThrownBy(() -> servicio.entregar(mariel, 10L, entrega(
        union(201L, 202L), union(202L, 302L), union(203L, 303L), union(204L, 304L))))
        .isInstanceOf(RespuestaInvalidaException.class)
        .hasMessage("Cada ítem debe unirse con uno de la columna derecha");
    verify(respuestaRepository, never()).saveAll(any());
  }
}
