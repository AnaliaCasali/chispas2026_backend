package edu.ies63.chispas2026_backend.models;

import org.hibernate.annotations.ColumnDefault;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "capacidad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Capacidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "area", nullable = false, length = 60)
    private String area;

    @Column(name = "pregunta", nullable = false, length = 255)
    private String pregunta;

    @Builder.Default
    @ColumnDefault("1")
    @Column(name = "orden", nullable = false)
    private Integer orden = 1;
}
