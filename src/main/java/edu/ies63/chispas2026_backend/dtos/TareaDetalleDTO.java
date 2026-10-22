package edu.ies63.chispas2026_backend.dtos;

import java.util.List;

// lo que necesita el front para abrir una tarea desde la lista y resolverla:
// materia (banner), contenido (Clase seleccionada) y la actividad con sus items.
// Los itemId son los que se mandan despues en POST /tareas/{id}/entrega
public record TareaDetalleDTO(
    Long tareaId,
    String estado,
    boolean realizada,
    MateriaTareaDTO materia,
    ContenidoTareaDTO contenido,
    Long actividadId,
    String titulo,
    String consigna,
    TipoActividadDTO tipoActividad,
    List<ItemActividadDTO> items
) {
}
