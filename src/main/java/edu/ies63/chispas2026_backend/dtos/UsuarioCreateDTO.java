package edu.ies63.chispas2026_backend.dtos;

import jakarta.validation.constraints.*;

// para usuario comun
public record UsuarioCreateDTO(
    // username sera email 
    
    @Email(message = "Debe ingresar un correo electrónico válido")
    @Size(max = 120, message = "El email debe tener como máximo 120 caracteres")
    String email,

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, max = 20, message = "La contraseña debe tener entre " +
        "8 y 20 caracteres")
    String password,

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 60, message = "El nombre debe tener entre 2 y 60 caracteres")
    String nombre,

    @NotBlank (message = "El apellido no puede estar vacío")
    @Size(min = 2, max = 60, message = "El apellido debe tener entre 2 y 60 caracteres")
    String apellido,

    @NotBlank (message = "El DNI no puede estar vacío")
    @Size(min = 8, max = 12, message = "El DNI debe tener entre 8 y 12 caracteres")
    String dni,

    String fotoUrl

) {

}
