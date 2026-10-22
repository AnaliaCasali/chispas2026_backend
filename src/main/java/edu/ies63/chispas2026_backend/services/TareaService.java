package edu.ies63.chispas2026_backend.services;

import edu.ies63.chispas2026_backend.dtos.EntregaRequestDTO;
import edu.ies63.chispas2026_backend.dtos.HistorialTareaDTO;
import edu.ies63.chispas2026_backend.dtos.ResultadoTareaDTO;
import edu.ies63.chispas2026_backend.models.Usuario;

public interface TareaService {
    ResultadoTareaDTO entregar(Usuario estudiante, Long tareaId, EntregaRequestDTO entrega);

    HistorialTareaDTO historial(Usuario estudiante, Long tareaId);
}
