package edu.ies63.chispas2026_backend.models;

import org.hibernate.annotations.ColumnDefault;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// una carpeta por alumno en cada ciclo lectivo
@Entity
@Table(name = "carpeta", uniqueConstraints = @UniqueConstraint(name = "uq_carpeta",
        columnNames = {"estudiante_id", "ciclo_lectivo_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carpeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ciclo_lectivo_id", nullable = false)
    private CicloLectivo cicloLectivo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @Column(name = "nombre", nullable = false, length = 60)
    private String nombre;

    @Column(name = "color", columnDefinition = "CHAR(7)")
    private String color;

    @CreationTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP(6)")
    @Column(name = "creada_en", nullable = false, updatable = false)
    private LocalDateTime creadaEn;
}
