package edu.ies63.chispas2026_backend.models;

import org.hibernate.annotations.ColumnDefault;
import edu.ies63.chispas2026_backend.enums.OrigenImagen;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// las imagenes no se guardan en la bd, solo donde estan
// EXTERNA = url completa, LOCAL = ruta relativa
@Entity
@Table(name = "imagen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Imagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen", nullable = false)
    private OrigenImagen origen;

    @Column(name = "ubicacion", nullable = false, unique = true, length = 255)
    private String ubicacion;

    @Column(name = "nombre", length = 80)
    private String nombre;

    // para el lector de pantalla
    @Column(name = "texto_alt", nullable = false, length = 160)
    private String textoAlt;

    @Column(name = "mime_type", length = 30)
    private String mimeType;

    @Column(name = "tamanio_kb")
    private Integer tamanioKb;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subida_por", nullable = false)
    private Usuario subidaPor;

    @CreationTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP(6)")
    @Column(name = "subida_en", nullable = false, updatable = false)
    private LocalDateTime subidaEn;
}
