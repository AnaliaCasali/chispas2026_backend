package edu.ies63.chispas2026_backend.models;

import org.hibernate.annotations.ColumnDefault;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "ciclo_lectivo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CicloLectivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "anio", nullable = false, unique = true)
    private Integer anio;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Builder.Default
    @ColumnDefault("false")
    @Column(name = "activo", nullable = false)
    private boolean activo = false;
}
