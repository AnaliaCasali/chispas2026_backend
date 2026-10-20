package edu.ies63.chispas2026_backend.services.impl;

import edu.ies63.chispas2026_backend.dtos.ActividadCarpetaDTO;
import edu.ies63.chispas2026_backend.dtos.InicioDTO;
import edu.ies63.chispas2026_backend.dtos.MateriaCarpetaDTO;
import edu.ies63.chispas2026_backend.dtos.MateriaInicioDTO;
import edu.ies63.chispas2026_backend.dtos.MiCarpetaDTO;
import edu.ies63.chispas2026_backend.models.Tarea;
import edu.ies63.chispas2026_backend.models.Usuario;
import edu.ies63.chispas2026_backend.repositories.TareaRepository;
import edu.ies63.chispas2026_backend.services.CarpetaService;
import edu.ies63.chispas2026_backend.services.InicioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

// TSDS-6: arma el Inicio reutilizando la carpeta de Santi (materias ordenadas y etiquetadas
// segun el horario) y le suma a cada materia sus tareas pendientes.
// El horario solo ordena y destaca: el alumno ve todas sus materias aunque este en su casa
@Service
@Transactional(readOnly = true)
public class InicioServiceImpl implements InicioService {

  static final String MSG_SIN_PENDIENTES = "No tienes actividades pendientes en esta materia";

  private final CarpetaService carpetaService;
  private final TareaRepository tareaRepository;
  private final Clock clock;

  public InicioServiceImpl(CarpetaService carpetaService, TareaRepository tareaRepository, Clock clock) {
    this.carpetaService = carpetaService;
    this.tareaRepository = tareaRepository;
    this.clock = clock;
  }

  @Override
  public InicioDTO obtenerInicio(Usuario estudiante) {
    // materias ya ordenadas: AHORA, despues HOY por hora y despues el resto
    MiCarpetaDTO carpeta = carpetaService.obtenerMiCarpeta(estudiante);

    // todas las pendientes de la carpeta en una sola consulta, agrupadas por materia
    // (el LinkedHashMap y toList mantienen el orden del repositorio: la mas nueva primero)
    LocalDate hoy = LocalDate.now(clock);
    Map<Long, List<ActividadCarpetaDTO>> pendientesPorMateria = tareaRepository
        .findPendientesHastaHoy(carpeta.carpetaId(), hoy)
        .stream()
        .collect(groupingBy(
            tarea -> tarea.getCarpetaEspacio().getId(),
            LinkedHashMap::new,
            mapping((Tarea tarea) -> CarpetaServiceImpl.toActividadDTO(tarea, hoy), toList())));

    List<MateriaInicioDTO> materias = carpeta.materias().stream()
        .map(materia -> toMateriaInicioDTO(materia,
            pendientesPorMateria.getOrDefault(materia.carpetaEspacioId(), List.of())))
        .toList();

    return new InicioDTO(carpeta.hayClaseAhora(), carpeta.mensaje(), materias);
  }

  // la primera pendiente es la principal y no se repite en otrasPendientes
  private MateriaInicioDTO toMateriaInicioDTO(MateriaCarpetaDTO materia, List<ActividadCarpetaDTO> pendientes) {
    boolean sinPendientes = pendientes.isEmpty();
    return new MateriaInicioDTO(
        materia.carpetaEspacioId(),
        materia.espacioCurricularId(),
        materia.nombre(),
        materia.color(),
        materia.icono(),
        materia.etiqueta(),
        materia.horaInicio(),
        materia.horaFin(),
        sinPendientes ? null : pendientes.get(0),
        sinPendientes ? List.of() : pendientes.subList(1, pendientes.size()),
        sinPendientes ? MSG_SIN_PENDIENTES : null
    );
  }
}
