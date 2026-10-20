package edu.ies63.chispas2026_backend.models;

import jakarta.persistence.*;
import lombok.*;

// respuesta correcta de UNIR_FLECHAS, una fila por par
@Entity
@Table(name = "actividad_par", uniqueConstraints = {
        @UniqueConstraint(name = "uq_par_izq", columnNames = {"actividad_id", "item_izq_id"}),
        @UniqueConstraint(name = "uq_par_der", columnNames = {"actividad_id", "item_der_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActividadPar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actividad_id", nullable = false)
    private Actividad actividad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_izq_id", nullable = false)
    private ActividadImagen itemIzq;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_der_id", nullable = false)
    private ActividadImagen itemDer;
}
