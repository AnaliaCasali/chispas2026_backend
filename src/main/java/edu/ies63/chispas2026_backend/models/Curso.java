package edu.ies63.chispas2026_backend.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "curso", uniqueConstraints = @UniqueConstraint(name = "uq_curso",
        columnNames = {"ciclo_lectivo_id", "nombre", "division"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ciclo_lectivo_id", nullable = false)
    private CicloLectivo cicloLectivo;

    @Column(name = "nombre", nullable = false, length = 30)
    private String nombre;

    @Column(name = "division", length = 10)
    private String division;
}
