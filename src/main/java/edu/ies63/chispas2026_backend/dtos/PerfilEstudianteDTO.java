package edu.ies63.chispas2026_backend.dtos;

import java.util.List;

// condiciones nunca es null: si no tiene ninguna va [] y el mensaje
public record PerfilEstudianteDTO(
    Long id,
    String nombre,
    String apellido,
    String fotoUrl,
    String curso,
    List<String> condiciones,
    String mensaje
) {
}
