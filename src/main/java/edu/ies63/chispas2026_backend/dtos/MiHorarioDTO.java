package edu.ies63.chispas2026_backend.dtos;

import edu.ies63.chispas2026_backend.enums.DiaSemana;

import java.util.List;

// horario de hoy del alumno, ordenado por hora. Sabado y domingo dia va en null.
// Si hoy no tiene clases se manda el mensaje y la lista vacia
public record MiHorarioDTO(
    DiaSemana dia,
    String mensaje,
    List<ClaseHorarioDTO> clases
) {
}
