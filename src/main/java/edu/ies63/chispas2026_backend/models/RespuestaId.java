package edu.ies63.chispas2026_backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RespuestaId implements Serializable {

    @Column(name = "tarea_id")
    private Long tareaId;

    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "intento")
    private Integer intento;
}
