package edu.ies63.chispas2026_backend.security.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "El email o DNI es obligatorio")
        String username,
        @NotBlank(message = "La contraseña es obligatoria")
        String password) {}
