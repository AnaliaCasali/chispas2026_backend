package edu.ies63.chispas2026_backend.services;

import edu.ies63.chispas2026_backend.dtos.MiHorarioDTO;
import edu.ies63.chispas2026_backend.models.Usuario;

public interface HorarioService {
  MiHorarioDTO obtenerMiHorario(Usuario estudiante);
}
