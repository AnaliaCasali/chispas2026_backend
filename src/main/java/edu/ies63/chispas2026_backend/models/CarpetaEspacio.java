package edu.ies63.chispas2026_backend.models;

import jakarta.persistence.*;
import lombok.*;

// cada seccion de la carpeta es una materia
@Entity
@Table(name = "carpeta_espacio", uniqueConstraints = @UniqueConstraint(name = "uq_carpeta_espacio",
        columnNames = {"carpeta_id", "espacio_curricular_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarpetaEspacio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carpeta_id", nullable = false)
    private Carpeta carpeta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "espacio_curricular_id", nullable = false)
    private EspacioCurricular espacioCurricular;

    // orden en que se muestran en Mis Materias
    @Builder.Default
    @Column(name = "orden", nullable = false)
    private Integer orden = 1;
}
