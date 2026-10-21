package edu.ies63.chispas2026_backend.services.impl;

import edu.ies63.chispas2026_backend.dtos.EstudianteResumenDTO;
import edu.ies63.chispas2026_backend.dtos.PerfilEstudianteDTO;
import edu.ies63.chispas2026_backend.enums.Role;
import edu.ies63.chispas2026_backend.exceptions.AccesoDenegadoException;
import edu.ies63.chispas2026_backend.models.Carpeta;
import edu.ies63.chispas2026_backend.models.Condicion;
import edu.ies63.chispas2026_backend.models.Curso;
import edu.ies63.chispas2026_backend.models.Estudiante;
import edu.ies63.chispas2026_backend.models.Usuario;
import edu.ies63.chispas2026_backend.repositories.CarpetaRepository;
import edu.ies63.chispas2026_backend.repositories.EstudianteRepository;
import edu.ies63.chispas2026_backend.services.EstudianteService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Perfil del estudiante: el alumno solo ve el suyo y el docente solo el de sus alumnos
@Service
@Transactional(readOnly = true)
public class EstudianteServiceImpl implements EstudianteService {

  static final String MSG_SIN_CONDICION = "Sin condición registrada";
  static final String MSG_SIN_PERMISO = "No tienes permiso para ver este perfil";

  private final EstudianteRepository estudianteRepository;
  private final CarpetaRepository carpetaRepository;

  public EstudianteServiceImpl(EstudianteRepository estudianteRepository, CarpetaRepository carpetaRepository) {
    this.estudianteRepository = estudianteRepository;
    this.carpetaRepository = carpetaRepository;
  }

  @Override
  public PerfilEstudianteDTO obtenerPerfil(Usuario usuario, Long estudianteId) {
    // el alumno no puede ver otro perfil, ni siquiera saber si existe
    if (usuario.getRole() == Role.ESTUDIANTE && !usuario.getId().equals(estudianteId)) {
      throw new AccesoDenegadoException(MSG_SIN_PERMISO);
    }

    Estudiante estudiante = estudianteRepository.findConCondicionesById(estudianteId)
        .orElseThrow(() -> new EntityNotFoundException("El estudiante no existe"));

    if (usuario.getRole() == Role.DOCENTE && !carpetaRepository.esAlumnoDelDocente(estudianteId, usuario.getId())) {
      throw new AccesoDenegadoException(MSG_SIN_PERMISO);
    }

    // si no tiene carpeta este año el perfil se muestra igual, sin curso
    String curso = carpetaRepository.findByEstudianteIdAndCicloLectivoActivoTrue(estudianteId)
        .map(carpeta -> nombreCurso(carpeta.getCurso()))
        .orElse(null);

    List<String> condiciones = estudiante.getCondiciones().stream()
        .map(Condicion::getNombre)
        .sorted()
        .toList();

    Usuario datos = estudiante.getUsuario();
    return new PerfilEstudianteDTO(
        estudiante.getId(),
        datos.getNombre(),
        datos.getApellido(),
        datos.getFotoUrl(),
        curso,
        condiciones,
        condiciones.isEmpty() ? MSG_SIN_CONDICION : null
    );
  }

  @Override
  public List<EstudianteResumenDTO> listarMisAlumnos(Usuario docente) {
    return carpetaRepository.findDelCicloActivoPorDocente(docente.getId()).stream()
        .map(this::toResumenDTO)
        .toList();
  }

  private EstudianteResumenDTO toResumenDTO(Carpeta carpeta) {
    Usuario alumno = carpeta.getEstudiante().getUsuario();
    return new EstudianteResumenDTO(
        carpeta.getEstudiante().getId(),
        alumno.getNombre(),
        alumno.getApellido(),
        nombreCurso(carpeta.getCurso())
    );
  }

  // "4to Año A", o solo el nombre si no tiene division
  private String nombreCurso(Curso curso) {
    return curso.getDivision() == null ? curso.getNombre() : curso.getNombre() + " " + curso.getDivision();
  }
}
