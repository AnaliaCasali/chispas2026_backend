package edu.ies63.chispas2026_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import edu.ies63.chispas2026_backend.models.Respuesta;
import edu.ies63.chispas2026_backend.models.RespuestaId;

@Repository
public interface RespuestaRepository extends JpaRepository<Respuesta, RespuestaId> {

List<Respuesta> findByTareaIdAndIdIntento(Long tareaId, Integer intento);
 
List<Respuesta> findByTareaIdOrderByIdIntentoAscItemIdAsc(Long tareaId);

@Query("SELECT COALESCE(MAX(r.id.intento), 0) FROM Respuesta r WHERE r.tarea.id = :tareaId")
int maxIntentoPorTarea(@Param("tareaId") Long tareaId);

}
