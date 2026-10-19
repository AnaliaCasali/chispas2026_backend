package edu.ies63.chispas2026_backend.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "estudiante_capacidad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstudianteCapacidad {

    @EmbeddedId
    @Builder.Default
    private EstudianteCapacidadId id = new EstudianteCapacidadId();

    @MapsId("estudianteId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id")
    private Estudiante estudiante;

    @MapsId("capacidadId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "capacidad_id")
    private Capacidad capacidad;

    @Column(name = "respuesta", nullable = false)
    private boolean respuesta;

    @UpdateTimestamp
    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;
}
