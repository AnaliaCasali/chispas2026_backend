package edu.ies63.chispas2026_backend.repositories;

import edu.ies63.chispas2026_backend.models.Estudiante;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    // trae el usuario y las condiciones para armar el perfil
    @EntityGraph(attributePaths = {"usuario", "condiciones"})
    Optional<Estudiante> findConCondicionesById(Long id);
}
