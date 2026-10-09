package edu.ies63.chispas2026_backend.repositories;

import edu.ies63.chispas2026_backend.models.PasswordResetToken;
import edu.ies63.chispas2026_backend.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUsuario(Usuario usuario);
}