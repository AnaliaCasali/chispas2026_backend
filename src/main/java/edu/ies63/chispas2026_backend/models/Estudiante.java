package edu.ies63.chispas2026_backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

// usa el mismo id que usuario (usuario_id es PK y FK)
@Entity
@Table(name = "estudiante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estudiante {

    @Id
    @Column(name = "usuario_id")
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    // puede no tener ninguna condicion
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "estudiante_condicion",
            joinColumns = @JoinColumn(name = "estudiante_id"),
            inverseJoinColumns = @JoinColumn(name = "condicion_id"))
    private Set<Condicion> condiciones = new HashSet<>();
}
