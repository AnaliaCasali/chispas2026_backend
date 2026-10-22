package edu.ies63.chispas2026_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import edu.ies63.chispas2026_backend.models.ActividadImagen;

@Repository
public interface ActividadImagenRepository extends JpaRepository<ActividadImagen, Long> {

List<ActividadImagen> findByActividadId(Long actividadId);

// items con su imagen en una sola consulta, para mostrar la actividad
@Query("SELECT ai FROM ActividadImagen ai JOIN FETCH ai.imagen WHERE ai.actividad.id = :actividadId")
List<ActividadImagen> findConImagenByActividadId(Long actividadId);

}
