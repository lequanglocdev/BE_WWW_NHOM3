package fit.iuh.se.repositories;

import fit.iuh.se.entities.Cart;
import fit.iuh.se.entities.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(UserAccount user);
}
