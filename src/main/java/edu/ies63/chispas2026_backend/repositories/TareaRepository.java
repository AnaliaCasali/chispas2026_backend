package edu.ies63.chispas2026_backend.repositories;

import edu.ies63.chispas2026_backend.models.Tarea;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    @EntityGraph(attributePaths = {"actividad"})
    List<Tarea> findByCarpetaEspacioIdOrderByFechaAsignacionDescIdDesc(Long carpetaEspacioId);
}
