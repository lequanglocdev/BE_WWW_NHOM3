package fit.iuh.se.services;

import fit.iuh.se.dtos.ApiResponse;
import fit.iuh.se.dtos.CheckoutResponseDTO;
import fit.iuh.se.dtos.OrderRequest;
import fit.iuh.se.entities.*;
import fit.iuh.se.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired private CartRepository cartRepo;
    @Autowired private OrderRepository orderRepo;
    @Autowired private ProductRepository productRepo;
    @Autowired private UserAccountRepository userRepo;
    @Autowired private VNPayService vnPayService;

    // =====================================================
    //   CHECKOUT (placeOrder)
    // =====================================================

    /**
     * Đặt hàng - toàn bộ thực hiện trong một Transaction.
     * Nếu bất kỳ bước nào thất bại, tất cả sẽ được Rollback.
     *
     * Thứ tự thực hiện:
     *  1. Kiểm tra giỏ hàng
     *  2. Kiểm tra stock từng sản phẩm
     *  3. Tính tổng tiền
     *  4. Tạo & lưu Order
     *  5. Tạo & lưu từng OrderItem
     *  6. Trừ stock (CHỈ sau khi đã lưu thành công Order + OrderItems)
     *  7. Xóa giỏ hàng
     *  8. Tạo payment URL nếu là VNPAY
     */
    @Transactional
    public CheckoutResponseDTO placeOrder(String email, OrderRequest request) {

        // 1. Lấy user từ email (JWT cung cấp)
        UserAccount user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản: " + email));

        // 2. Lấy giỏ hàng và kiểm tra có hàng không
        Cart cart = cartRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng đang trống, vui lòng thêm sản phẩm trước khi đặt hàng");
        }

        // 3. Kiểm tra stock sản phẩm VÀ tính tổng tiền trước
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem ci : cart.getItems()) {
            Product p = ci.getProduct();
            if (p.getStock() < ci.getQuantity()) {
                throw new RuntimeException(
                        "Sản phẩm \"" + p.getName() + "\" không đủ tồn kho. " +
                        "Còn lại: " + p.getStock() + ", yêu cầu: " + ci.getQuantity()
                );
            }
            totalAmount = totalAmount.add(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }

        // 4. Tính tiền giảm giá và tiền cuối
        BigDecimal discountAmount = cart.getDiscountAmount() != null
                ? BigDecimal.valueOf(cart.getDiscountAmount()) : BigDecimal.ZERO;

        BigDecimal finalAmount = (cart.getFinalAmount() != null && cart.getFinalAmount() > 0)
                ? BigDecimal.valueOf(cart.getFinalAmount())
                : totalAmount.subtract(discountAmount);

        // 5. Tạo Order và xác định status theo phương thức thanh toán
        Order order = new Order();
        order.setUser(user);
        order.setCustomerName(request.getCustomerName() != null
                ? request.getCustomerName() : user.getFullName());
        order.setPhone(request.getPhone() != null
                ? request.getPhone() : user.getPhone());
        order.setShippingAddress(request.getShippingAddress() != null
                ? request.getShippingAddress() : user.getAddress());
        order.setOrderDate(LocalDateTime.now());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus(request.getPaymentMethod() == PaymentMethod.VNPAY
                ? OrderStatus.UNPAID : OrderStatus.PENDING);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setFinalAmount(finalAmount);
        order.setPromoCode(cart.getPromotion() != null ? cart.getPromotion().getCode() : null);

        // 6. Lưu Order trước (để có ID)
        orderRepo.save(order);

        // 7. Tạo và lưu từng OrderItem (snapshot giá tại thời điểm đặt)
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem ci : cart.getItems()) {
            Product p = ci.getProduct();

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(p);
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(p.getPrice()); // <-- Snapshot giá hiện tại

            orderItems.add(oi);
        }
        order.setItems(orderItems);
        orderRepo.save(order); // Lưu lại cùng với items (cascade)

        // 8. CHỈ trừ stock SAU KHI Order và OrderItem đã lưu thành công
        for (CartItem ci : cart.getItems()) {
            Product p = ci.getProduct();
            p.setStock(p.getStock() - ci.getQuantity());
            productRepo.save(p);
        }

        // 9. Xóa toàn bộ CartItem sau khi chốt đơn
        cart.getItems().clear();
        cart.setPromotion(null);
        cart.setDiscountAmount(0.0);
        cart.setFinalAmount(0.0);
        cartRepo.save(cart);

        // 10. Tạo Payment URL nếu là VNPAY
        String paymentUrl = null;
        if (request.getPaymentMethod() == PaymentMethod.VNPAY) {
            paymentUrl = vnPayService.createPaymentUrl(order.getId(), finalAmount);
        }

        return new CheckoutResponseDTO(
                order.getId(),
                request.getPaymentMethod() == PaymentMethod.VNPAY
                        ? "Đơn hàng đã tạo, vui lòng thanh toán qua VNPay"
                        : "Đặt hàng thành công, chờ xác nhận",
                paymentUrl
        );
    }

    // =====================================================
    //   VNPAY CALLBACK - Cập nhật trạng thái đơn hàng
    // =====================================================

    @Transactional
    public ResponseEntity<?> handleVnpayCallback(String responseCode, String txnRef) {
        Long orderId;
        try {
            orderId = Long.parseLong(txnRef);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "vnp_TxnRef không hợp lệ: " + txnRef));
        }

        Order order = orderRepo.findById(orderId)
                .orElse(null);

        if (order == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Không tìm thấy đơn hàng #" + orderId));
        }

        if ("00".equals(responseCode)) {
            order.setStatus(OrderStatus.PAID);
            orderRepo.save(order);
            return ResponseEntity.ok(new ApiResponse(true,
                    "Thanh toán thành công! Đơn hàng #" + orderId + " đã được xác nhận."));
        } else {
            // Các mã lỗi phổ biến của VNPay
            String reason = getVnpayErrorMessage(responseCode);
            return ResponseEntity.ok(new ApiResponse(false,
                    "Thanh toán thất bại. Mã lỗi: " + responseCode + " - " + reason));
        }
    }

    private String getVnpayErrorMessage(String code) {
        return switch (code) {
            case "07" -> "Trừ tiền thành công nhưng giao dịch bị nghi ngờ";
            case "09" -> "Thẻ/Tài khoản chưa đăng ký dịch vụ";
            case "10" -> "Xác thực thông tin thẻ quá 3 lần";
            case "11" -> "Hết hạn chờ thanh toán";
            case "12" -> "Thẻ/Tài khoản bị khóa";
            case "13" -> "Sai mật khẩu OTP";
            case "24" -> "Khách hàng hủy giao dịch";
            case "51" -> "Tài khoản không đủ số dư";
            case "65" -> "Vượt hạn mức giao dịch trong ngày";
            case "75" -> "Ngân hàng thanh toán đang bảo trì";
            case "79" -> "Nhập sai mật khẩu quá số lần quy định";
            default   -> "Lỗi không xác định";
        };
    }

    // =====================================================
    //   CÁC CHỨC NĂNG QUẢN LÝ ĐƠN HÀNG
    // =====================================================

    public ResponseEntity<?> myOrders(String email) {
        UserAccount user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        return ResponseEntity.ok(orderRepo.findByUser(user));
    }

    public ResponseEntity<?> orderDetail(String email, Long orderId) {
        UserAccount user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng #" + orderId));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền xem đơn hàng này");
        }

        return ResponseEntity.ok(order);
    }

    public ResponseEntity<?> getAllOrders() {
        return ResponseEntity.ok(orderRepo.findAll());
    }

    public ResponseEntity<?> updateStatus(Long orderId, String status) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng #" + orderId));

        try {
            order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái không hợp lệ: " + status +
                    ". Các giá trị hợp lệ: PENDING, UNPAID, PAID, CANCELLED");
        }

        orderRepo.save(order);
        return ResponseEntity.ok(new ApiResponse(true, "Cập nhật trạng thái thành công"));
    }

    @Transactional
    public ResponseEntity<?> cancelOrder(String email, Long orderId) {
        UserAccount user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng #" + orderId));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền hủy đơn hàng này");
        }

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.UNPAID) {
            throw new RuntimeException("Chỉ có thể hủy đơn hàng ở trạng thái PENDING hoặc UNPAID");
        }

        // Hoàn lại stock khi hủy đơn
        for (OrderItem item : order.getItems()) {
            Product p = item.getProduct();
            p.setStock(p.getStock() + item.getQuantity());
            productRepo.save(p);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepo.save(order);

        return ResponseEntity.ok(new ApiResponse(true, "Hủy đơn hàng #" + orderId + " thành công"));
    }
}
