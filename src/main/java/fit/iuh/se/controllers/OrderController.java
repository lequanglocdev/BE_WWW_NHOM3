package fit.iuh.se.controllers;

import fit.iuh.se.dtos.CheckoutResponseDTO;
import fit.iuh.se.dtos.OrderRequest;
import fit.iuh.se.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    // =====================================================
    //   CHECKOUT
    //   POST /api/user/checkout
    // =====================================================

    /**
     * Đặt hàng: nhận OrderRequest (paymentMethod, customerName, phone, shippingAddress).
     * Lấy email user hiện tại từ JWT qua SecurityContextHolder.
     */
    @PostMapping("/user/checkout")
    public ResponseEntity<?> checkout(Authentication auth,
                                      @RequestBody OrderRequest request) {
        String email = auth.getName(); // Email lấy từ JWT
        CheckoutResponseDTO response = orderService.placeOrder(email, request);
        return ResponseEntity.ok(response);
    }

    // =====================================================
    //   VNPAY CALLBACK
    //   GET /api/payment/vnpay-callback
    //
    //   VNPay sẽ redirect đến URL này sau khi thanh toán.
    //   Để test thủ công, dán URL vào trình duyệt với:
    //     ?vnp_ResponseCode=00&vnp_TxnRef={orderId}
    //
    //   Ví dụ: http://localhost:8081/api/payment/vnpay-callback?vnp_ResponseCode=00&vnp_TxnRef=1
    // =====================================================

    @GetMapping("/payment/vnpay-callback")
    public ResponseEntity<?> vnpayCallback(
            @RequestParam("vnp_ResponseCode") String responseCode,
            @RequestParam("vnp_TxnRef") String txnRef) {

        return orderService.handleVnpayCallback(responseCode, txnRef);
    }

    // =====================================================
    //   CÁC CHỨC NĂNG USER
    //   Prefix: /api/user/orders
    // =====================================================

    @GetMapping("/user/orders")
    public ResponseEntity<?> myOrders(Authentication auth) {
        return orderService.myOrders(auth.getName());
    }

    @GetMapping("/user/orders/{id}")
    public ResponseEntity<?> orderDetail(Authentication auth,
                                         @PathVariable Long id) {
        return orderService.orderDetail(auth.getName(), id);
    }

    @DeleteMapping("/user/orders/{id}/cancel")
    public ResponseEntity<?> cancelOrder(Authentication auth,
                                         @PathVariable Long id) {
        return orderService.cancelOrder(auth.getName(), id);
    }
}