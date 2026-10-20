package edu.ies63.chispas2026_backend.dtos;

public record ResultadoTareaDTO(

      Long tareaId,
      int aciertos,
      int totalItems,
      String mensaje,
      String estado,
      boolean puedeIntentar
) {

}
