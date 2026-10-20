package edu.ies63.chispas2026_backend.dtos;

import java.time.LocalDate;

// estado Pendiente o Realizada
// TSDS-4: se cambia "Completada" por "Realizada" y se agregan tipo de actividad,
// imagen (portada del contenido), realizada (true si esta FINALIZADA) y esHoy (etiqueta HOY)
public record ActividadCarpetaDTO(
    Long tareaId,
    Long actividadId,
    String titulo,
    TipoActividadDTO tipoActividad,
    String imagenUrl,
    LocalDate fechaAsignacion,
    String estado,
    boolean realizada,
    boolean esHoy
) {
}
