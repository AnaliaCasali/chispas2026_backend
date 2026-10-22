package edu.ies63.chispas2026_backend.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;


import edu.ies63.chispas2026_backend.dtos.ContenidoTareaDTO;
import edu.ies63.chispas2026_backend.dtos.EntregaRequestDTO;
import edu.ies63.chispas2026_backend.dtos.ItemActividadDTO;
import edu.ies63.chispas2026_backend.dtos.ItemRespuestaDTO;
import edu.ies63.chispas2026_backend.dtos.MateriaTareaDTO;
import edu.ies63.chispas2026_backend.dtos.ResultadoTareaDTO;
import edu.ies63.chispas2026_backend.dtos.TareaDetalleDTO;
import edu.ies63.chispas2026_backend.dtos.TipoActividadDTO;
import edu.ies63.chispas2026_backend.enums.CodigoPlantilla;
import edu.ies63.chispas2026_backend.enums.EstadoTarea;
import edu.ies63.chispas2026_backend.enums.LadoItem;
import edu.ies63.chispas2026_backend.exceptions.AccesoDenegadoException;
import edu.ies63.chispas2026_backend.exceptions.RespuestaInvalidaException;
import edu.ies63.chispas2026_backend.exceptions.TareaYaFinalizadaException;
import edu.ies63.chispas2026_backend.models.Actividad;
import edu.ies63.chispas2026_backend.models.ActividadImagen;
import edu.ies63.chispas2026_backend.models.Contenido;
import edu.ies63.chispas2026_backend.models.EspacioCurricular;
import edu.ies63.chispas2026_backend.models.Imagen;
import edu.ies63.chispas2026_backend.models.Plantilla;
import edu.ies63.chispas2026_backend.models.Respuesta;
import edu.ies63.chispas2026_backend.models.Tarea;
import edu.ies63.chispas2026_backend.models.Usuario;
import edu.ies63.chispas2026_backend.repositories.ActividadImagenRepository;
import edu.ies63.chispas2026_backend.repositories.ActividadParRepository;
import edu.ies63.chispas2026_backend.repositories.RespuestaRepository;
import edu.ies63.chispas2026_backend.repositories.TareaRepository;
import edu.ies63.chispas2026_backend.services.TareaService;
import java.time.Clock;
import java.time.Duration;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service 
public class TareaServiceImpl implements TareaService {

  static final String MSG_TODO_BIEN    = "¡Felicitaciones respondiste todo correctamente!";
  static final String MSG_MUY_CERCA    = "¡Estuviste muy cerca! ¡Inténtalo de nuevo!";
  static final String MSG_BUEN_INTENTO = "¡Buen intento! Cada vez que practicás, aprendés algo nuevo. ¡Volvé a intentarlo!";
  static final String MSG_SIN_PERMISO = "No tienes permiso para responder esta tarea";
  static final String MSG_YA_FINALIZADA = "Ya respondiste correctamente esta actividad";
  static final String MSG_NO_EXISTE = "La tarea solicitada no existe";
  static final String MSG_SIN_PERMISO_VER = "No tienes permiso para ver esta tarea";

  private final TareaRepository tareaRepository;
  private final RespuestaRepository respuestaRepository;
  private final ActividadImagenRepository actividadImagenRepository;
  private final ActividadParRepository actividadParRepository;
  private final Clock clock;

  TareaServiceImpl(TareaRepository tareaRepository,
                  RespuestaRepository respuestaRepository,
                  ActividadImagenRepository actividadImagenRepository,
                  ActividadParRepository actividadParRepository,
                  Clock clock) {
    this.tareaRepository = tareaRepository;
    this.respuestaRepository = respuestaRepository;
    this.actividadImagenRepository = actividadImagenRepository;
    this.actividadParRepository = actividadParRepository;
    this.clock = clock;
  }


  @Override
  @Transactional
  public TareaDetalleDTO obtenerDetalle(Usuario estudiante, Long tareaId) {
    // una tarea con fecha futura todavia no esta asignada: igual que en la lista, para el alumno no existe
    LocalDate hoy = LocalDate.now(clock);
    Tarea tarea = tareaRepository.findDetalleById(tareaId)
        .filter(t -> !t.getFechaAsignacion().isAfter(hoy))
        .orElseThrow(() -> new EntityNotFoundException(MSG_NO_EXISTE));

    // el id del estudiante es el mismo que el de su usuario
    if (!tarea.getCarpetaEspacio().getCarpeta().getEstudiante().getId().equals(estudiante.getId())) {
      throw new AccesoDenegadoException(MSG_SIN_PERMISO_VER);
    }

    Actividad actividad = tarea.getActividad();
    Plantilla plantilla = actividad.getPlantilla();
    boolean esUnirFlechas = plantilla.getCodigo() == CodigoPlantilla.UNIR_FLECHAS;

    // ORDENAR: se mezclan, porque el orden de la base es la respuesta.
    // UNIR_FLECHAS: primero la columna izquierda y despues la derecha, cada una en su orden
    List<ActividadImagen> items = new ArrayList<>(actividadImagenRepository.findConImagenByActividadId(actividad.getId()));
    if (esUnirFlechas) {
      items.sort(Comparator.comparing(ActividadImagen::getLado).thenComparing(ActividadImagen::getNroOrden));
    } else {
      Collections.shuffle(items);
    }

    EspacioCurricular espacio = tarea.getCarpetaEspacio().getEspacioCurricular();
    Contenido contenido = actividad.getContenido();
    boolean realizada = tarea.getEstado() == EstadoTarea.FINALIZADA;
    return new TareaDetalleDTO(
        tarea.getId(),
        realizada ? "Realizada" : "Pendiente",
        realizada,
        new MateriaTareaDTO(tarea.getCarpetaEspacio().getId(), espacio.getNombre(), espacio.getColor(), espacio.getIcono()),
        new ContenidoTareaDTO(contenido.getTema(), contenido.getTexto(), contenido.getPortadaUrl(), contenido.getAudioUrl()),
        actividad.getId(),
        actividad.getTitulo(),
        actividad.getConsigna(),
        new TipoActividadDTO(plantilla.getCodigo(), plantilla.getNombre()),
        items.stream().map(item -> toItemDTO(item, esUnirFlechas)).toList()
    );
  }

  // en ORDENAR no se manda el nroOrden porque es la posicion correcta
  private ItemActividadDTO toItemDTO(ActividadImagen item, boolean esUnirFlechas) {
    Imagen imagen = item.getImagen();
    return new ItemActividadDTO(
        item.getId(),
        imagen.getUbicacion(),
        imagen.getTextoAlt(),
        item.getTitulo(),
        item.getLado(),
        esUnirFlechas ? item.getNroOrden() : null
    );
  }

  @Override
  @Transactional
  public ResultadoTareaDTO entregar(Usuario estudiante, Long tareaId, EntregaRequestDTO entrega) {

    Tarea tarea = tareaRepository.findCompletaById(tareaId)
        .orElseThrow(() -> new EntityNotFoundException(MSG_NO_EXISTE));

    validarDuenio(tarea, estudiante);

    
    if (tarea.getEstado() == EstadoTarea.FINALIZADA) {
      throw new TareaYaFinalizadaException(MSG_YA_FINALIZADA);
    }

    Actividad actividad = tarea.getActividad();
    boolean esUnirFlechas = actividad.getPlantilla().getCodigo() == CodigoPlantilla.UNIR_FLECHAS;

    // todos los items de la actividad, para buscar los que une el alumno
    Map<Long, ActividadImagen> itemsPorId = actividadImagenRepository.findByActividadId(actividad.getId())
        .stream()
        .collect(Collectors.toMap(ActividadImagen::getId, Function.identity()));
        

    // los que el alumno debe responder: todos en ORDENAR
    Map<Long, ActividadImagen> respondiblesPorId = itemsPorId.values().stream()
        .filter(item -> !esUnirFlechas || item.getLado() == LadoItem.IZQ)
        .collect(Collectors.toMap(ActividadImagen::getId, Function.identity()));

    // en UNIR_FLECHAS
    Map<Long, Long> parCorrectoPorIzq = esUnirFlechas ? paresCorrectos(actividad.getId()) : Map.of();

    validarEntrega(entrega, respondiblesPorId, itemsPorId, esUnirFlechas);

    // intento nuevo: el historial nunca se borra, cada entrega es un INSERT mas
    int intento = respuestaRepository.maxIntentoPorTarea(tareaId) + 1;
    List<Respuesta> respuestas = new ArrayList<>(entrega.items().size());
    for (ItemRespuestaDTO itemRespuesta : entrega.items()) {
      ActividadImagen item = respondiblesPorId.get(itemRespuesta.itemId());
      boolean esCorrecta = esUnirFlechas
          ? itemRespuesta.itemUnidoId().equals(parCorrectoPorIzq.get(item.getId()))
          : itemRespuesta.posicionElegida().equals(item.getNroOrden());

      Respuesta respuesta = Respuesta.builder()
          .tarea(tarea)
          .item(item)
          .posicionElegida(esUnirFlechas ? null : itemRespuesta.posicionElegida())
          .itemUnido(esUnirFlechas ? itemsPorId.get(itemRespuesta.itemUnidoId()) : null)
          .esCorrecta(esCorrecta)
          .build();
      // el numero de intento va en la clave; tareaId e itemId los completa @MapsId
      respuesta.getId().setIntento(intento);
      respuestas.add(respuesta);
    }
    respuestaRepository.saveAll(respuestas);

    int aciertos = (int) respuestas.stream().filter(Respuesta::isEsCorrecta).count();
    int totalItems = respuestas.size();

    actualizarTarea(tarea, aciertos, totalItems);

    return resultado(tarea, aciertos, totalItems);
  }

  // la tarea tiene que ser de la carpeta del alumno logueado (sale del token)
  private void validarDuenio(Tarea tarea, Usuario estudiante) {
    Usuario duenio = tarea.getCarpetaEspacio().getCarpeta().getEstudiante().getUsuario();
    if (!duenio.getId().equals(estudiante.getId())) {
      throw new AccesoDenegadoException(MSG_SIN_PERMISO);
    }
  }

  // mapa itemIzq -> itemDer correcto, segun la tabla actividad_par
  private Map<Long, Long> paresCorrectos(Long actividadId) {
    return actividadParRepository.findConItemsByActividadId(actividadId).stream()
        .collect(Collectors.toMap(
            par -> par.getItemIzq().getId(),
            par -> par.getItemDer().getId()));
  }

  // una respuesta por item de la actividad, sin repetidos y con el dato que pide la plantilla
  private void validarEntrega(EntregaRequestDTO entrega,
                              Map<Long, ActividadImagen> respondiblesPorId,
                              Map<Long, ActividadImagen> itemsPorId,
                              boolean esUnirFlechas) {
    if (entrega.items().size() != respondiblesPorId.size()) {
      throw new RespuestaInvalidaException("Debes responder todos los ítems de la actividad");
    }

    Set<Long> vistos = new HashSet<>();
    for (ItemRespuestaDTO itemRespuesta : entrega.items()) {
      if (!respondiblesPorId.containsKey(itemRespuesta.itemId())
          || !vistos.add(itemRespuesta.itemId())) {
        throw new RespuestaInvalidaException("Cada ítem de la actividad se responde una sola vez");
      }
      if (esUnirFlechas) {
        ActividadImagen unido = itemRespuesta.itemUnidoId() == null
            ? null
            : itemsPorId.get(itemRespuesta.itemUnidoId());
        if (unido == null || unido.getLado() != LadoItem.DER) {
          throw new RespuestaInvalidaException("Cada ítem debe unirse con uno de la columna derecha");
        }
      } else if (itemRespuesta.posicionElegida() == null) {
        throw new RespuestaInvalidaException("Cada ítem debe tener la posición elegida");
      }
    }
  }

  // la tarea guarda el resultado del ultimo intento; inicia y finaliza una sola vez
  private void actualizarTarea(Tarea tarea, int aciertos, int totalItems) {
    LocalDateTime ahora = LocalDateTime.now(clock);

    if (tarea.getIniciadaEn() == null) {
      tarea.setIniciadaEn(ahora);
    }
    tarea.setAciertos(aciertos);
    tarea.setTotalItems(totalItems);

    if (aciertos == totalItems) {
      tarea.setFinalizadaEn(ahora);
      tarea.setDuracionMinutos((int) Duration.between(tarea.getIniciadaEn(), ahora).toMinutes());
      tarea.setEstado(EstadoTarea.FINALIZADA);
    } else {
      tarea.setEstado(EstadoTarea.EN_CURSO);
    }
    tareaRepository.save(tarea);
  }

  // mensaje segun aciertos: todo bien, muy cerca (mitad o mas) o buen intento
  private ResultadoTareaDTO resultado(Tarea tarea, int aciertos, int totalItems) {
    boolean acertoTodo = aciertos == totalItems;
    String mensaje = acertoTodo
        ? MSG_TODO_BIEN
        : aciertos >= (totalItems + 1) / 2   // ceil(total/2): 2 o 3 de 4 → muy cerca
            ? MSG_MUY_CERCA
            : MSG_BUEN_INTENTO;

    return new ResultadoTareaDTO(
        tarea.getId(),
        aciertos,
        totalItems,
        mensaje,
        acertoTodo ? "Realizada" : "Pendiente",
        !acertoTodo
    );
  }





 
  }



 

