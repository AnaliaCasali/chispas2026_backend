package edu.ies63.chispas2026_backend.dto;


import edu.ies63.chispas2026_backend.enums.Role;

public record UsuarioResponseDTO(
    Long id,
    String username,
    Role role,
    String nombre
) {
}
