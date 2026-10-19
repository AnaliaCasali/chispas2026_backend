package edu.ies63.chispas2026_backend.repositories;

 import edu.ies63.chispas2026_backend.enums.Role;
 import edu.ies63.chispas2026_backend.models.Usuario;
 import org.springframework.data.jpa.repository.JpaRepository;
 import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByDni(String dni);
    Optional<Usuario> findByEmailOrDni(String email, String dni);
    List<Usuario> findByActivoTrueAndRoleIn(Collection<Role> roles);
}
