package edu.ies63.chispas2026_backend.repositories;

import edu.ies63.chispas2026_backend.models.Carpeta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarpetaRepository extends JpaRepository<Carpeta, Long> {

    // busca la carpeta del año actual
    @EntityGraph(attributePaths = {"curso"})
    Optional<Carpeta> findByEstudianteIdAndCicloLectivoActivoTrue(Long estudianteId);

    // un alumno es del docente si su carpeta del año actual es de un curso donde el docente tiene horario
    @Query("""
        SELECT COUNT(c) > 0 FROM Carpeta c
        WHERE c.estudiante.id = :estudianteId
          AND c.cicloLectivo.activo = true
          AND EXISTS (SELECT h FROM Horario h WHERE h.curso = c.curso AND h.docente.id = :docenteId)
        """)
    boolean esAlumnoDelDocente(Long estudianteId, Long docenteId);

    // carpetas del año actual de los alumnos del docente, ordenadas por apellido y nombre
    @Query("""
        SELECT c FROM Carpeta c
        JOIN FETCH c.estudiante e
        JOIN FETCH e.usuario u
        JOIN FETCH c.curso
        WHERE c.cicloLectivo.activo = true
          AND EXISTS (SELECT h FROM Horario h WHERE h.curso = c.curso AND h.docente.id = :docenteId)
        ORDER BY u.apellido, u.nombre, u.id
        """)
    List<Carpeta> findDelCicloActivoPorDocente(Long docenteId);
}
