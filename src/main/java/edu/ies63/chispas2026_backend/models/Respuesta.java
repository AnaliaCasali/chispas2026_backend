package edu.ies63.chispas2026_backend.models;

import jakarta.persistence.*;
import lombok.*;

// una fila por cada item que respondio el alumno
// ORDENAR usa posicionElegida, UNIR_FLECHAS usa itemUnido
@Entity
@Table(name = "respuesta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Respuesta {

    @EmbeddedId
    @Builder.Default
    private RespuestaId id = new RespuestaId();

    @MapsId("tareaId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tarea_id")
    private Tarea tarea;

    @MapsId("itemId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id")
    private ActividadImagen item;

    @Column(name = "posicion_elegida")
    private Integer posicionElegida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_unido_id")
    private ActividadImagen itemUnido;

    @Column(name = "es_correcta", nullable = false)
    private boolean esCorrecta;
}
