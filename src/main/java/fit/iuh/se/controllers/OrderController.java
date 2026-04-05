package fit.iuh.se.controllers;

import fit.iuh.se.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/orders")
public class OrderController {

    @Autowired
    private OrderService service;

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(Authentication auth) {
        return service.checkout(auth.getName());
    }

    @GetMapping("/my-orders")
    public ResponseEntity<?> myOrders(Authentication auth) {
        return service.myOrders(auth.getName());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(Authentication auth,
                                    @PathVariable Long id) {
        return service.orderDetail(auth.getName(), id);
    }
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(Authentication auth,
                                         @PathVariable Long id) {
        return service.cancelOrder(auth.getName(), id);
    }
}