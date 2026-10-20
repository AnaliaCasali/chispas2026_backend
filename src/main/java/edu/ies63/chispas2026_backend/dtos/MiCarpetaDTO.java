package edu.ies63.chispas2026_backend.dtos;

import java.util.List;

// si no tiene clase ahora se manda el mensaje
public record MiCarpetaDTO(
    Long carpetaId,
    String nombre,
    String color,
    boolean hayClaseAhora,
    String mensaje,
    List<MateriaCarpetaDTO> materias
) {
}
