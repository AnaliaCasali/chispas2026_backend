package edu.ies63.chispas2026_backend.dtos;

import java.time.LocalTime;

// una clase del horario de hoy. carpetaEspacioId es para ir a la materia desde el horario
// y ahora es true si la clase esta en curso
public record ClaseHorarioDTO(
    Long carpetaEspacioId,
    Long espacioCurricularId,
    String materia,
    String color,
    String icono,
    LocalTime horaInicio,
    LocalTime horaFin,
    boolean ahora
) {
}
