package edu.ies63.chispas2026_backend.dtos;

import java.util.List;

public record HistorialTareaDTO(
    Long tareaId,
    String estado,
    ResultadoTareaDTO ultimoResultado,
    int intentoActual,
    int cantidadIntentos,
    List<IntentoResumenDTO>intentos
) {

}
