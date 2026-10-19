package edu.ies63.chispas2026_backend.services;

import edu.ies63.chispas2026_backend.dtos.ActividadesMateriaDTO;
import edu.ies63.chispas2026_backend.dtos.MiCarpetaDTO;
import edu.ies63.chispas2026_backend.models.Usuario;

public interface CarpetaService {
  MiCarpetaDTO obtenerMiCarpeta(Usuario estudiante);

  ActividadesMateriaDTO obtenerActividadesDeMateria(Usuario estudiante, Long carpetaEspacioId);
}
