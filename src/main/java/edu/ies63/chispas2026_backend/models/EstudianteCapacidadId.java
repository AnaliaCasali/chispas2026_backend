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
public class EstudianteCapacidadId implements Serializable {

    @Column(name = "estudiante_id")
    private Long estudianteId;

    @Column(name = "capacidad_id")
    private Long capacidadId;
}
