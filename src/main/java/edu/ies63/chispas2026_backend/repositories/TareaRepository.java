package edu.ies63.chispas2026_backend.repositories;

import edu.ies63.chispas2026_backend.models.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

   
   //para corregir la entrega de la tarea.
    @Query("""
        SELECT t FROM Tarea t
          JOIN FETCH t.actividad a 
          JOIN FETCH a.plantilla
          LEFT JOIN FETCH t.carpetaEspacio ce 
          LEFT JOIN FETCH ce.carpeta c 
          LEFT JOIN FETCH c.estudiante e
          LEFT JOIN FETCH e.usuario 
          WHERE t.id = :tareaId
        """)
        Optional<Tarea> findCompletaById(@Param("tareaId") Long tareaId);

    // para abrir la tarea: actividad con plantilla y contenido, y la materia con su carpeta
    // (para el banner y para validar que sea del alumno logueado)
    @Query("""
        SELECT t FROM Tarea t
          JOIN FETCH t.actividad a
          JOIN FETCH a.plantilla
          JOIN FETCH a.contenido
          JOIN FETCH t.carpetaEspacio ce
          JOIN FETCH ce.espacioCurricular
          JOIN FETCH ce.carpeta
         WHERE t.id = :tareaId
        """)
    Optional<Tarea> findDetalleById(Long tareaId);



    // TSDS-6: pendientes (todo lo que no esta FINALIZADA, EN_CURSO incluida) de todas las
    // materias de una carpeta, asignadas hasta hoy. Mismo orden que findAsignadasHastaHoy,
    // asi la primera de cada materia es la actividad principal del Inicio
    @Query("""
        SELECT t FROM Tarea t
          JOIN FETCH t.actividad a
          JOIN FETCH a.plantilla
          JOIN FETCH a.contenido
         WHERE t.carpetaEspacio.carpeta.id = :carpetaId
           AND t.fechaAsignacion <= :hoy
           AND t.estado <> edu.ies63.chispas2026_backend.enums.EstadoTarea.FINALIZADA
         ORDER BY t.fechaAsignacion DESC, t.asignadaEn DESC, t.id DESC
        """)
    List<Tarea> findPendientesHastaHoy(Long carpetaId, LocalDate hoy);
}
