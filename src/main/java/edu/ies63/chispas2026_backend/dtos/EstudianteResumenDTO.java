package edu.ies63.chispas2026_backend.dtos;

// item de la lista de alumnos del docente
public record EstudianteResumenDTO(
    Long id,
    String nombre,
    String apellido,
    String curso
) {
}
