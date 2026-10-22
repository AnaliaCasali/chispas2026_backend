package edu.ies63.chispas2026_backend.dtos;

// contenido de la actividad (pantalla Clase seleccionada): tema, texto, portada
// y el audio para el boton Escuchar audiolibro (puede venir en null)
public record ContenidoTareaDTO(
    String tema,
    String texto,
    String portadaUrl,
    String audioUrl
) {
}
