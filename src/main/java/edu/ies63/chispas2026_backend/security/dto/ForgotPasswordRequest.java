package edu.ies63.chispas2026_backend.security.dto;

import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank(message = "El email o username es obligatorio")
        String email
) {}