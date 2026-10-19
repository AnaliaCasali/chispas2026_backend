package edu.ies63.chispas2026_backend.dtos;

import java.util.List;

// si no hay actividades se manda el mensaje
public record ActividadesMateriaDTO(
    Long carpetaEspacioId,
    String materia,
    String color,
    String icono,
    String mensaje,
    List<ActividadCarpetaDTO> actividades
) {
}
