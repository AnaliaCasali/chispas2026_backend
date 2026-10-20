package edu.ies63.chispas2026_backend.dtos;

import edu.ies63.chispas2026_backend.enums.EtiquetaMateria;

import java.time.LocalTime;

// si la materia no es hoy, etiqueta y horas van en null
public record MateriaCarpetaDTO(
    Long carpetaEspacioId,
    Long espacioCurricularId,
    String nombre,
    String color,
    String icono,
    EtiquetaMateria etiqueta,
    LocalTime horaInicio,
    LocalTime horaFin
) {
}
