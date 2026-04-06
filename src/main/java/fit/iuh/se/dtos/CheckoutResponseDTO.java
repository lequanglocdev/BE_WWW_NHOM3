package fit.iuh.se.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO trả về sau khi checkout thành công.
 * - orderId: ID đơn hàng vừa tạo.
 * - paymentUrl: URL thanh toán VNPay (null nếu là COD).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutResponseDTO {

    private Long orderId;
    private String message;
    private String paymentUrl; // null nếu phương thức là COD
}
