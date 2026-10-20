package edu.ies63.chispas2026_backend.models;

import edu.ies63.chispas2026_backend.enums.EstadoTarea;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tarea",
        uniqueConstraints = @UniqueConstraint(name = "uq_tarea",
                columnNames = {"actividad_id", "carpeta_espacio_id", "fecha_asignacion"}),
        indexes = @Index(name = "ix_tarea_ce_fecha", columnList = "carpeta_espacio_id, fecha_asignacion"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actividad_id", nullable = false)
    private Actividad actividad;

    // carpeta del alumno + materia
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carpeta_espacio_id", nullable = false)
    private CarpetaEspacio carpetaEspacio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asignada_por", nullable = false)
    private Usuario asignadaPor;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDate fechaAsignacion;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoTarea estado = EstadoTarea.PENDIENTE;

    @Column(name = "iniciada_en")
    private LocalDateTime iniciadaEn;

    @Column(name = "finalizada_en")
    private LocalDateTime finalizadaEn;

    @Column(name = "duracion_minutos")
    private Integer duracionMinutos;

    @Column(name = "aciertos")
    private Integer aciertos;

    @Column(name = "total_items")
    private Integer totalItems;
}
