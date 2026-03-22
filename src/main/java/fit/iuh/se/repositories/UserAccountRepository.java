package fit.iuh.se.repositories;

import fit.iuh.se.entities.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> {
    boolean existsByEmail(String email);
    Optional<UserAccount> findByEmail(String email);
}
