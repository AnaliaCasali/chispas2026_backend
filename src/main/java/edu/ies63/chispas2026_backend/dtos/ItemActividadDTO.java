package edu.ies63.chispas2026_backend.dtos;

import edu.ies63.chispas2026_backend.enums.LadoItem;

// una imagen de la actividad. textoAlt es para el lector de pantalla.
// En ORDENAR posicion va en null (es la respuesta) y los items llegan mezclados;
// en UNIR_FLECHAS posicion es el lugar en su columna (IZQ o DER)
public record ItemActividadDTO(
    Long itemId,
    String imagenUrl,
    String textoAlt,
    String titulo,
    LadoItem lado,
    Integer posicion
) {
}
