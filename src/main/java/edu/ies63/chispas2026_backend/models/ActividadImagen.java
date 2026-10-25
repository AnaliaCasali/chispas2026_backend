package edu.ies63.chispas2026_backend.models;

import edu.ies63.chispas2026_backend.enums.LadoItem;
import jakarta.persistence.*;
import lombok.*;

// cada imagen de la actividad
// en ORDENAR nroOrden es la posicion correcta, en UNIR_FLECHAS la posicion en su columna
// en UNIR_FLECHAS una misma imagen puede ir en IZQ y en DER (ej. imagen con su frase)
@Entity
@Table(name = "actividad_imagen", uniqueConstraints = {
        @UniqueConstraint(name = "uq_ai_imagen", columnNames = {"actividad_id", "imagen_id", "lado"}),
        @UniqueConstraint(name = "uq_ai_orden", columnNames = {"actividad_id", "lado", "nro_orden"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActividadImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actividad_id", nullable = false)
    private Actividad actividad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "imagen_id", nullable = false)
    private Imagen imagen;

    // texto que se ve con la imagen, ej "Sale de su casa"
    @Column(name = "titulo", length = 80)
    private String titulo;

    @Column(name = "nro_orden", nullable = false)
    private Integer nroOrden;

    // IZQ y DER solo se usan en UNIR_FLECHAS
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "lado", nullable = false)
    private LadoItem lado = LadoItem.UNICA;
}
