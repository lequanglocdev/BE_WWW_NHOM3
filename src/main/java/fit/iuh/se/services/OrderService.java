package fit.iuh.se.services;

import fit.iuh.se.dtos.ApiResponse;
import fit.iuh.se.entities.*;
import fit.iuh.se.repositories.CartRepository;
import fit.iuh.se.repositories.OrderRepository;
import fit.iuh.se.repositories.ProductRepository;
import fit.iuh.se.repositories.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    @Autowired
    private CartRepository cartRepo;
    @Autowired private OrderRepository orderRepo;
    @Autowired private ProductRepository productRepo;
    @Autowired private UserAccountRepository userRepo;

    public ResponseEntity<?> checkout(String email) {
        UserAccount user = userRepo.findByEmail(email).orElseThrow();

        if (user.getFullName() == null || user.getPhone() == null || user.getAddress() == null)
            throw new RuntimeException("Vui lòng cập nhật đầy đủ thông tin");

        Cart cart = cartRepo.findByUser(user).orElseThrow();

        Order order = new Order();
        order.setUser(user);
        order.setReceiverName(user.getFullName());
        order.setPhone(user.getPhone());
        order.setAddress(user.getAddress());
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("PENDING");

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem ci : cart.getItems()) {
            Product p = ci.getProduct();
            if (p.getStock() < ci.getQuantity())
                throw new RuntimeException("Sản phẩm " + p.getName() + " không đủ hàng");

            p.setStock(p.getStock() - ci.getQuantity());
            productRepo.save(p);

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(p);
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(p.getPrice());

            total = total.add(p.getPrice()
                    .multiply(BigDecimal.valueOf(ci.getQuantity())));

            items.add(oi);
        }

        order.setItems(items);
        order.setTotalAmount(total);
        order.setDiscountAmount(cart.getDiscountAmount() != null ? cart.getDiscountAmount() : 0.0);
        order.setFinalAmount(cart.getFinalAmount() != null && cart.getFinalAmount() > 0
                ? cart.getFinalAmount()
                : total.doubleValue());
        order.setPromoCode(cart.getPromotion() != null ? cart.getPromotion().getCode() : null);


        orderRepo.save(order);

        cart.getItems().clear();
        cart.setPromotion(null);
        cart.setDiscountAmount(0.0);
        cart.setFinalAmount(0.0);
        cartRepo.save(cart);

        Map<String, Object> response = new HashMap<>();
        response.put("status", true);
        response.put("message", "Đặt hàng thành công");
        response.put("orderId", order.getId());
        response.put("totalAmount", total);
        response.put("discountAmount", order.getDiscountAmount()); // ✅ thêm
        response.put("finalAmount", order.getFinalAmount());       // ✅ thêm
        response.put("totalItems", items.size());
        response.put("createdAt", order.getCreatedAt());

        return ResponseEntity.ok(response);
    }
    public ResponseEntity<?> myOrders(String email) {
        UserAccount user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        return ResponseEntity.ok(orderRepo.findByUser(user));
    }

    public ResponseEntity<?> orderDetail(String email, Long orderId) {
        UserAccount user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!order.getUser().getId().equals(user.getId()))
            throw new RuntimeException("Bạn không có quyền xem đơn này");

        return ResponseEntity.ok(order);
    }

    public ResponseEntity<?> getAllOrders() {
        return ResponseEntity.ok(orderRepo.findAll());
    }

    public ResponseEntity<?> updateStatus(Long orderId, String status) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        order.setStatus(status.toUpperCase());
        orderRepo.save(order);

        return ResponseEntity.ok(new ApiResponse(true, "Cập nhật trạng thái thành công"));
    }
    public ResponseEntity<?> cancelOrder(String email, Long orderId) {
        UserAccount user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!order.getUser().getId().equals(user.getId()))
            throw new RuntimeException("Bạn không có quyền hủy đơn này");

        if (!order.getStatus().equals("PENDING"))
            throw new RuntimeException("Chỉ có thể hủy đơn khi đang chờ xác nhận");

        // Hoàn lại stock
        for (OrderItem item : order.getItems()) {
            Product p = item.getProduct();
            p.setStock(p.getStock() + item.getQuantity());
            productRepo.save(p);
        }

        order.setStatus("CANCELLED");
        orderRepo.save(order);

        return ResponseEntity.ok(new ApiResponse(true, "Hủy đơn hàng thành công"));
    }
}
