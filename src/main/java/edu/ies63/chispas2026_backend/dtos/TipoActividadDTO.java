package edu.ies63.chispas2026_backend.dtos;

import edu.ies63.chispas2026_backend.enums.CodigoPlantilla;

// TSDS-4: tipo de actividad de una tarea (codigo de la plantilla y su nombre visible)
public record TipoActividadDTO(
    CodigoPlantilla codigo,
    String nombre
) {
}
