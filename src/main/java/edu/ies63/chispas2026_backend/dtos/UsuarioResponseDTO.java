package edu.ies63.chispas2026_backend.dtos;


import edu.ies63.chispas2026_backend.enums.Role;

public record UsuarioResponseDTO(
    Long id,
    String email,
    String nombre,
    String apellido,
    String dni,
    String fotoUrl,
    Role role,
    boolean activo
) {
}
