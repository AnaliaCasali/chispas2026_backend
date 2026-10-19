package edu.ies63.chispas2026_backend.repositories;

import edu.ies63.chispas2026_backend.models.Carpeta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarpetaRepository extends JpaRepository<Carpeta, Long> {

    // busca la carpeta del año actual
    @EntityGraph(attributePaths = {"curso"})
    Optional<Carpeta> findByEstudianteIdAndCicloLectivoActivoTrue(Long estudianteId);
}
