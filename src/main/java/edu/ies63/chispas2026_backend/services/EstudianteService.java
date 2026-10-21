package edu.ies63.chispas2026_backend.services;

import edu.ies63.chispas2026_backend.dtos.EstudianteResumenDTO;
import edu.ies63.chispas2026_backend.dtos.PerfilEstudianteDTO;
import edu.ies63.chispas2026_backend.models.Usuario;

import java.util.List;

public interface EstudianteService {
  PerfilEstudianteDTO obtenerPerfil(Usuario usuario, Long estudianteId);

  List<EstudianteResumenDTO> listarMisAlumnos(Usuario docente);
}
