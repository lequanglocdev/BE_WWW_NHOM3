package fit.iuh.se.dtos;

import fit.iuh.se.entities.PaymentMethod;
import lombok.Data;

/**
 * DTO nhận dữ liệu từ client khi checkout.
 */
@Data
public class OrderRequest {

    private PaymentMethod paymentMethod; // COD hoặc VNPAY

    private String customerName;     // Tên người nhận
    private String phone;            // SĐT người nhận
    private String shippingAddress;  // Địa chỉ giao hàng
}
