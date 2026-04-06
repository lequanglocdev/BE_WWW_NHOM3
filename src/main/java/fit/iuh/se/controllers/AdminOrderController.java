package fit.iuh.se.controllers;

import fit.iuh.se.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<?> allOrders() {
        return orderService.getAllOrders();
    }

    /**
     * Cập nhật trạng thái đơn hàng.
     * Giá trị hợp lệ cho status: PENDING, UNPAID, PAID, CANCELLED
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @RequestParam String status) {
        return orderService.updateStatus(id, status);
    }
}