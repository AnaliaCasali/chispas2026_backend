package edu.ies63.chispas2026_backend.repository;

 import edu.ies63.chispas2026_backend.enums.Role;
 import edu.ies63.chispas2026_backend.model.Usuario;
 import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    List<Usuario> findByDebajaFalseAndRoleIn(Collection<Role> roles);
}
