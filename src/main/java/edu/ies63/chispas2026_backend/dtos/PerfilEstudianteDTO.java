package edu.ies63.chispas2026_backend.dtos;

import java.util.List;

// condiciones y capacidades nunca son null: si no tiene ninguna va []
// si no tiene condiciones va tambien el mensaje
public record PerfilEstudianteDTO(
    Long id,
    String nombre,
    String apellido,
    String fotoUrl,
    String curso,
    List<String> condiciones,
    String mensaje,
    String observaciones,
    List<CapacidadEstudianteDTO> capacidades
) {
}
