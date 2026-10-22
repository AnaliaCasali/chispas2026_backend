package edu.ies63.chispas2026_backend.dtos;

// materia de la tarea, para el banner de la pantalla de la actividad
public record MateriaTareaDTO(
    Long carpetaEspacioId,
    String nombre,
    String color,
    String icono
) {
}
