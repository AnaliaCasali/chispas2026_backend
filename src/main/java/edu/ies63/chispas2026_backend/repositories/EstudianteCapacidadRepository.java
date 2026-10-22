package edu.ies63.chispas2026_backend.repositories;

import edu.ies63.chispas2026_backend.models.EstudianteCapacidad;
import edu.ies63.chispas2026_backend.models.EstudianteCapacidadId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstudianteCapacidadRepository extends JpaRepository<EstudianteCapacidad, EstudianteCapacidadId> {

    // trae la capacidad junto para no hacer una consulta por cada respuesta
    @Query("""
        SELECT ec FROM EstudianteCapacidad ec JOIN FETCH ec.capacidad c
        WHERE ec.estudiante.id = :estudianteId
        ORDER BY c.area, c.orden
        """)
    List<EstudianteCapacidad> findDelEstudiante(Long estudianteId);
}
