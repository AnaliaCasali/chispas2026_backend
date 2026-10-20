package edu.ies63.chispas2026_backend.repositories;

import edu.ies63.chispas2026_backend.enums.DiaSemana;
import edu.ies63.chispas2026_backend.models.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, Long> {

    List<Horario> findByCursoIdAndDiaOrderByHoraInicioAsc(Long cursoId, DiaSemana dia);
}
