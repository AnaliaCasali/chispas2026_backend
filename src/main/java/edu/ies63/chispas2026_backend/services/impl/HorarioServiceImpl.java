package edu.ies63.chispas2026_backend.services.impl;

import edu.ies63.chispas2026_backend.dtos.ClaseHorarioDTO;
import edu.ies63.chispas2026_backend.dtos.MiHorarioDTO;
import edu.ies63.chispas2026_backend.enums.DiaSemana;
import edu.ies63.chispas2026_backend.models.Carpeta;
import edu.ies63.chispas2026_backend.models.CarpetaEspacio;
import edu.ies63.chispas2026_backend.models.EspacioCurricular;
import edu.ies63.chispas2026_backend.models.Horario;
import edu.ies63.chispas2026_backend.models.Usuario;
import edu.ies63.chispas2026_backend.repositories.CarpetaEspacioRepository;
import edu.ies63.chispas2026_backend.repositories.CarpetaRepository;
import edu.ies63.chispas2026_backend.repositories.HorarioRepository;
import edu.ies63.chispas2026_backend.services.HorarioService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

// pantalla Horarios: las clases de hoy del curso del alumno, ordenadas por hora
@Service
@Transactional(readOnly = true)
public class HorarioServiceImpl implements HorarioService {

  static final String MSG_SIN_CLASES_HOY = "Hoy no tienes clases";

  private final CarpetaRepository carpetaRepository;
  private final CarpetaEspacioRepository carpetaEspacioRepository;
  private final HorarioRepository horarioRepository;
  private final Clock clock;

  public HorarioServiceImpl(CarpetaRepository carpetaRepository,
                            CarpetaEspacioRepository carpetaEspacioRepository,
                            HorarioRepository horarioRepository,
                            Clock clock) {
    this.carpetaRepository = carpetaRepository;
    this.carpetaEspacioRepository = carpetaEspacioRepository;
    this.horarioRepository = horarioRepository;
    this.clock = clock;
  }

  @Override
  public MiHorarioDTO obtenerMiHorario(Usuario estudiante) {
    Carpeta carpeta = carpetaRepository.findByEstudianteIdAndCicloLectivoActivoTrue(estudiante.getId())
        .orElseThrow(() -> new EntityNotFoundException("No tienes una carpeta para el ciclo lectivo actual"));

    LocalDateTime ahora = LocalDateTime.now(clock);
    Optional<DiaSemana> hoy = DiaSemana.desde(ahora.getDayOfWeek());
    if (hoy.isEmpty()) {
      return new MiHorarioDTO(null, MSG_SIN_CLASES_HOY, List.of());
    }

    // las materias de la carpeta (vienen con su espacio curricular): solo se muestran las clases
    // de estas, porque el alumno puede no cursar todas las materias del curso.
    // Se cargan antes que los horarios, asi el espacio de cada horario ya esta en memoria
    Map<Long, CarpetaEspacio> seccionPorEspacio = carpetaEspacioRepository
        .findByCarpetaIdOrderByOrdenAsc(carpeta.getId())
        .stream()
        .collect(toMap(seccion -> seccion.getEspacioCurricular().getId(), seccion -> seccion));

    LocalTime hora = ahora.toLocalTime();
    List<ClaseHorarioDTO> clases = horarioRepository
        .findByCursoIdAndDiaOrderByHoraInicioAsc(carpeta.getCurso().getId(), hoy.get())
        .stream()
        .filter(h -> seccionPorEspacio.containsKey(h.getEspacioCurricular().getId()))
        .map(h -> toClaseDTO(h, seccionPorEspacio.get(h.getEspacioCurricular().getId()), hora))
        .toList();

    return new MiHorarioDTO(hoy.get(), clases.isEmpty() ? MSG_SIN_CLASES_HOY : null, clases);
  }

  private ClaseHorarioDTO toClaseDTO(Horario horario, CarpetaEspacio seccion, LocalTime hora) {
    EspacioCurricular espacio = seccion.getEspacioCurricular();
    boolean enCurso = !hora.isBefore(horario.getHoraInicio()) && hora.isBefore(horario.getHoraFin());
    return new ClaseHorarioDTO(
        seccion.getId(),
        espacio.getId(),
        espacio.getNombre(),
        espacio.getColor(),
        espacio.getIcono(),
        horario.getHoraInicio(),
        horario.getHoraFin(),
        enCurso
    );
  }
}
