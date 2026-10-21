package edu.ies63.chispas2026_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.ies63.chispas2026_backend.models.ActividadImagen;

@Repository
public interface ActividadImagenRepository extends JpaRepository<ActividadImagen, Long> {

List<ActividadImagen> findByActividadId(Long actividadId);

}
