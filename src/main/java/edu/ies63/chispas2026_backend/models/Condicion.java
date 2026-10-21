package edu.ies63.chispas2026_backend.models;

import jakarta.persistence.*;
import lombok.*;

// catalogo de condiciones medicas (TDA, dislexia, autismo, ...)
@Entity
@Table(name = "condicion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Condicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 60)
    private String nombre;
}
