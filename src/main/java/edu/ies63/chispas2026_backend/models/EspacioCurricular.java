package edu.ies63.chispas2026_backend.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "espacio_curricular")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EspacioCurricular {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 60)
    private String nombre;

    @Column(name = "icono", length = 255)
    private String icono;

    @Column(name = "color", columnDefinition = "CHAR(7)")
    private String color;
}
