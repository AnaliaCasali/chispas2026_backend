package edu.ies63.chispas2026_backend.repositories;

import edu.ies63.chispas2026_backend.models.CarpetaEspacio;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarpetaEspacioRepository extends JpaRepository<CarpetaEspacio, Long> {

    @EntityGraph(attributePaths = {"espacioCurricular"})
    List<CarpetaEspacio> findByCarpetaIdOrderByOrdenAsc(Long carpetaId);

    @Query("SELECT ce FROM CarpetaEspacio ce JOIN FETCH ce.espacioCurricular JOIN FETCH ce.carpeta WHERE ce.id = :id")
    Optional<CarpetaEspacio> findConEspacioYCarpetaById(Long id);
}
