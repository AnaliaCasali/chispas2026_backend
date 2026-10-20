package edu.ies63.chispas2026_backend.dtos;

import jakarta.validation.constraints.NotNull;

public record ItemRespuestaDTO(

      @NotNull Long itemId,
      Integer posicionElegida,
      Long itemUnidoId

) {}
