package edu.ies63.chispas2026_backend.dtos;


import edu.ies63.chispas2026_backend.enums.Role;

public record UsuarioResponseDTO(
    Long id,
    String username,
    Role role,
    String nombre
) {
}
