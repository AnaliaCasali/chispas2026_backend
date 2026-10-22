package edu.ies63.chispas2026_backend.dtos;

// una pregunta de capacidad respondida para el alumno
public record CapacidadEstudianteDTO(
    String area,
    String pregunta,
    boolean respuesta
) {
}
