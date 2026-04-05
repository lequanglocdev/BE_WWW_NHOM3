package fit.iuh.se.repositories;

import fit.iuh.se.entities.RefreshToken;
import fit.iuh.se.entities.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(UserAccount user);
}