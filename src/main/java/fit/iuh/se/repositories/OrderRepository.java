package fit.iuh.se.repositories;

import fit.iuh.se.entities.Order;
import fit.iuh.se.entities.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Lấy danh sách đơn hàng của 1 user
    List<Order> findByUser(UserAccount user);

}
