package fit.iuh.se.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private UserAccount user;

    // --- Thông tin người nhận ---
    private String customerName;
    private String phone;
    private String shippingAddress;

    // --- Thời gian & Trạng thái ---
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    // --- Tiền (tất cả BigDecimal) ---
    private BigDecimal totalAmount;      // Tổng tiền gốc
    private BigDecimal discountAmount;   // Số tiền được giảm
    private BigDecimal finalAmount;      // Tổng tiền sau giảm

    // --- Mã giảm giá ---
    private String promoCode;

    // --- Danh sách sản phẩm ---
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;
}