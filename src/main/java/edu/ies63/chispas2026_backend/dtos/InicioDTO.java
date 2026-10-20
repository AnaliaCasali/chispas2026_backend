package edu.ies63.chispas2026_backend.dtos;

import java.util.List;

// TSDS-6: resumen del Inicio del alumno. Trae todas las materias de la carpeta
// (la de ahora primero) con sus pendientes; si no tiene clase ahora se manda el mensaje
public record InicioDTO(
    boolean hayClaseAhora,
    String mensaje,
    List<MateriaInicioDTO> materias
) {
}
