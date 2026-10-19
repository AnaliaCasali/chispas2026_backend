package edu.ies63.chispas2026_backend.models;

import edu.ies63.chispas2026_backend.enums.CodigoPlantilla;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plantilla")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plantilla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "codigo", nullable = false, unique = true)
    private CodigoPlantilla codigo;

    @Column(name = "nombre", nullable = false, length = 60)
    private String nombre;

    @Column(name = "destinatario", length = 120)
    private String destinatario;

    // se validan en el service
    @Column(name = "min_items", nullable = false)
    private Integer minItems;

    @Column(name = "max_items", nullable = false)
    private Integer maxItems;
}
