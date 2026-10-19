package edu.ies63.chispas2026_backend.dtos;

import java.time.LocalDate;

// estado Pendiente o Completada
public record ActividadCarpetaDTO(
    Long tareaId,
    Long actividadId,
    String titulo,
    LocalDate fechaAsignacion,
    String estado
) {
}
