package edu.ies63.chispas2026_backend.repositories;

import edu.ies63.chispas2026_backend.models.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    // TSDS-4: reemplaza a findByCarpetaEspacioIdOrderByFechaAsignacionDescIdDesc.
    // Trae solo las tareas ya asignadas (fecha <= hoy), de la mas nueva a la mas vieja:
    // por fecha, despues por hora de asignacion y por id como ultimo desempate.
    // Trae actividad, plantilla y contenido en la misma consulta para no hacer N+1.
    @Query("""
        SELECT t FROM Tarea t
          JOIN FETCH t.actividad a
          JOIN FETCH a.plantilla
          JOIN FETCH a.contenido
         WHERE t.carpetaEspacio.id = :carpetaEspacioId
           AND t.fechaAsignacion <= :hoy
         ORDER BY t.fechaAsignacion DESC, t.asignadaEn DESC, t.id DESC
        """)
    List<Tarea> findAsignadasHastaHoy(Long carpetaEspacioId, LocalDate hoy);
}
