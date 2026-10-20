package edu.ies63.chispas2026_backend.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contenido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contenido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "espacio_curricular_id", nullable = false)
    private EspacioCurricular espacioCurricular;

    @Column(name = "tema", nullable = false, length = 120)
    private String tema;

    @Column(name = "texto", nullable = false, length = 255)
    private String texto;

    @Column(name = "portada_url", length = 255)
    private String portadaUrl;

    @Column(name = "audio_url", length = 255)
    private String audioUrl;
}
