package edu.ies63.chispas2026_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import edu.ies63.chispas2026_backend.models.ActividadPar;

@Repository
public interface ActividadParRepository extends JpaRepository<ActividadPar, Long>{

   @Query("""
      SELECT p FROM ActividadPar p
        JOIN FETCH p.itemIzq
        JOIN FETCH p.itemDer
       WHERE p.actividad.id = :actividadId
      """)
  List<ActividadPar> findConItemsByActividadId(Long actividadId);

}
