package edu.ies63.chispas2026_backend.dtos;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record EntregaRequestDTO(

    @NotEmpty @Valid List<ItemRespuestaDTO> items


) {

}
