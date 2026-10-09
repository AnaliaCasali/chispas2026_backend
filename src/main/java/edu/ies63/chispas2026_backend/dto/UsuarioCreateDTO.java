package edu.ies63.chispas2026_backend.dto;

import jakarta.validation.constraints.*;

// para usuario comun
public record UsuarioCreateDTO(
    // username sera email
    @NotNull(message = "El email no puede ser nulo")
    @Email(message = "Debe ingresar un correo electrónico válido")
    String username,

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, max = 20, message = "La contraseña debe tener entre " +
        "8 y 20 caracteres")
    String password,

    @NotBlank(message = "El nombre del grupo no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    String nombre

) {

}
