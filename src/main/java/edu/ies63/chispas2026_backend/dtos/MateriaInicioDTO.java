package edu.ies63.chispas2026_backend.dtos;

import edu.ies63.chispas2026_backend.enums.EtiquetaMateria;

import java.time.LocalTime;
import java.util.List;

// TSDS-6: una pantalla del Inicio (una materia). Mismos datos que en la carpeta,
// mas la actividad principal (la pendiente mas reciente) y el resto de sus pendientes.
// Si no tiene pendientes, actividadPrincipal va en null y se manda el mensaje
public record MateriaInicioDTO(
    Long carpetaEspacioId,
    Long espacioCurricularId,
    String nombre,
    String color,
    String icono,
    EtiquetaMateria etiqueta,
    LocalTime horaInicio,
    LocalTime horaFin,
    ActividadCarpetaDTO actividadPrincipal,
    List<ActividadCarpetaDTO> otrasPendientes,
    String mensaje
) {
}
