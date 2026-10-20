package edu.ies63.chispas2026_backend.services.impl;

import edu.ies63.chispas2026_backend.dtos.ActividadCarpetaDTO;
import edu.ies63.chispas2026_backend.dtos.ActividadesMateriaDTO;
import edu.ies63.chispas2026_backend.dtos.MateriaCarpetaDTO;
import edu.ies63.chispas2026_backend.dtos.MiCarpetaDTO;
import edu.ies63.chispas2026_backend.dtos.TipoActividadDTO;
import edu.ies63.chispas2026_backend.enums.DiaSemana;
import edu.ies63.chispas2026_backend.enums.EstadoTarea;
import edu.ies63.chispas2026_backend.enums.EtiquetaMateria;
import edu.ies63.chispas2026_backend.exceptions.AccesoDenegadoException;
import edu.ies63.chispas2026_backend.models.Actividad;
import edu.ies63.chispas2026_backend.models.Carpeta;
import edu.ies63.chispas2026_backend.models.CarpetaEspacio;
import edu.ies63.chispas2026_backend.models.EspacioCurricular;
import edu.ies63.chispas2026_backend.models.Horario;
import edu.ies63.chispas2026_backend.models.Plantilla;
import edu.ies63.chispas2026_backend.models.Tarea;
import edu.ies63.chispas2026_backend.models.Usuario;
import edu.ies63.chispas2026_backend.repositories.CarpetaEspacioRepository;
import edu.ies63.chispas2026_backend.repositories.CarpetaRepository;
import edu.ies63.chispas2026_backend.repositories.HorarioRepository;
import edu.ies63.chispas2026_backend.repositories.TareaRepository;
import edu.ies63.chispas2026_backend.services.CarpetaService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class CarpetaServiceImpl implements CarpetaService {

  static final String MSG_SIN_CLASES = "Ahora no tienes clases";
  static final String MSG_SIN_ACTIVIDADES = "Todavía no hay actividades en esta materia";
  static final String MSG_SIN_PERMISO = "No tienes permiso para ver esta carpeta";

  private final CarpetaRepository carpetaRepository;
  private final CarpetaEspacioRepository carpetaEspacioRepository;
  private final HorarioRepository horarioRepository;
  private final TareaRepository tareaRepository;
  private final Clock clock;

  public CarpetaServiceImpl(CarpetaRepository carpetaRepository,
                            CarpetaEspacioRepository carpetaEspacioRepository,
                            HorarioRepository horarioRepository,
                            TareaRepository tareaRepository,
                            Clock clock) {
    this.carpetaRepository = carpetaRepository;
    this.carpetaEspacioRepository = carpetaEspacioRepository;
    this.horarioRepository = horarioRepository;
    this.tareaRepository = tareaRepository;
    this.clock = clock;
  }

  @Override
  public MiCarpetaDTO obtenerMiCarpeta(Usuario estudiante) {
    Carpeta carpeta = carpetaRepository.findByEstudianteIdAndCicloLectivoActivoTrue(estudiante.getId())
        .orElseThrow(() -> new EntityNotFoundException("No tienes una carpeta para el ciclo lectivo actual"));

    List<CarpetaEspacio> secciones = carpetaEspacioRepository.findByCarpetaIdOrderByOrdenAsc(carpeta.getId());

    LocalDateTime ahora = LocalDateTime.now(clock);
    LocalTime hora = ahora.toLocalTime();
    List<Horario> horariosDeHoy = DiaSemana.desde(ahora.getDayOfWeek())
        .map(dia -> horarioRepository.findByCursoIdAndDiaOrderByHoraInicioAsc(carpeta.getCurso().getId(), dia))
        .orElse(List.of());

    // guardo el primer horario de cada materia, si alguna esta en curso uso ese
    Map<Long, Horario> bloquePorEspacio = new LinkedHashMap<>();
    Long espacioActualId = null;
    for (Horario h : horariosDeHoy) {
      Long espacioId = h.getEspacioCurricular().getId();
      boolean enCurso = !hora.isBefore(h.getHoraInicio()) && hora.isBefore(h.getHoraFin());
      if (enCurso) {
        espacioActualId = espacioId;
        bloquePorEspacio.put(espacioId, h);
      } else {
        bloquePorEspacio.putIfAbsent(espacioId, h);
      }
    }

    List<MateriaCarpetaDTO> ahoraLista = new ArrayList<>();
    List<MateriaCarpetaDTO> hoyLista = new ArrayList<>();
    List<MateriaCarpetaDTO> resto = new ArrayList<>();
    for (CarpetaEspacio seccion : secciones) {
      Long espacioId = seccion.getEspacioCurricular().getId();
      Horario bloque = bloquePorEspacio.get(espacioId);
      if (bloque == null) {
        resto.add(toMateriaDTO(seccion, null, null));
      } else if (espacioId.equals(espacioActualId)) {
        ahoraLista.add(toMateriaDTO(seccion, EtiquetaMateria.AHORA, bloque));
      } else {
        hoyLista.add(toMateriaDTO(seccion, EtiquetaMateria.HOY, bloque));
      }
    }
    hoyLista.sort(Comparator.comparing(MateriaCarpetaDTO::horaInicio));

    List<MateriaCarpetaDTO> materias = new ArrayList<>(ahoraLista);
    materias.addAll(hoyLista);
    materias.addAll(resto);

    boolean hayClaseAhora = !ahoraLista.isEmpty();
    return new MiCarpetaDTO(
        carpeta.getId(),
        carpeta.getNombre(),
        carpeta.getColor(),
        hayClaseAhora,
        hayClaseAhora ? null : MSG_SIN_CLASES,
        materias
    );
  }

  @Override
  public ActividadesMateriaDTO obtenerActividadesDeMateria(Usuario estudiante, Long carpetaEspacioId) {
    CarpetaEspacio seccion = carpetaEspacioRepository.findConEspacioYCarpetaById(carpetaEspacioId)
        .orElseThrow(() -> new EntityNotFoundException("La materia solicitada no existe"));

    // valido que la carpeta sea del alumno logueado
    if (!seccion.getCarpeta().getEstudiante().getId().equals(estudiante.getId())) {
      throw new AccesoDenegadoException(MSG_SIN_PERMISO);
    }

    // TSDS-4: "hoy" con la hora de Argentina; solo tareas asignadas hasta hoy,
    // ordenadas por fecha y hora de asignacion (la mas nueva primero)
    LocalDate hoy = LocalDate.now(clock);
    List<ActividadCarpetaDTO> actividades = tareaRepository
        .findAsignadasHastaHoy(carpetaEspacioId, hoy)
        .stream()
        .map(tarea -> toActividadDTO(tarea, hoy))
        .toList();

    EspacioCurricular espacio = seccion.getEspacioCurricular();
    return new ActividadesMateriaDTO(
        seccion.getId(),
        espacio.getNombre(),
        espacio.getColor(),
        espacio.getIcono(),
        actividades.isEmpty() ? MSG_SIN_ACTIVIDADES : null,
        actividades
    );
  }

  private MateriaCarpetaDTO toMateriaDTO(CarpetaEspacio seccion, EtiquetaMateria etiqueta, Horario bloque) {
    EspacioCurricular espacio = seccion.getEspacioCurricular();
    return new MateriaCarpetaDTO(
        seccion.getId(),
        espacio.getId(),
        espacio.getNombre(),
        espacio.getColor(),
        espacio.getIcono(),
        etiqueta,
        bloque != null ? bloque.getHoraInicio() : null,
        bloque != null ? bloque.getHoraFin() : null
    );
  }

  // TSDS-4: se agregan tipo de actividad, imagen (portada del contenido), realizada y esHoy.
  // "Completada" pasa a "Realizada"; EN_CURSO se muestra como Pendiente
  // TSDS-6: pasa a static y visible en el paquete para reutilizarlo en InicioServiceImpl
  static ActividadCarpetaDTO toActividadDTO(Tarea tarea, LocalDate hoy) {
    Actividad actividad = tarea.getActividad();
    Plantilla plantilla = actividad.getPlantilla();
    boolean realizada = tarea.getEstado() == EstadoTarea.FINALIZADA;
    return new ActividadCarpetaDTO(
        tarea.getId(),
        actividad.getId(),
        actividad.getTitulo(),
        new TipoActividadDTO(plantilla.getCodigo(), plantilla.getNombre()),
        actividad.getContenido().getPortadaUrl(),
        tarea.getFechaAsignacion(),
        realizada ? "Realizada" : "Pendiente",
        realizada,
        tarea.getFechaAsignacion().equals(hoy)
    );
  }
}
