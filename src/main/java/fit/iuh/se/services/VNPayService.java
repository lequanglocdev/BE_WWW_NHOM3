package fit.iuh.se.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * VNPayService - Tạo URL thanh toán VNPay.
 *
 * Trong môi trường phát triển / demo, URL này được mock để có thể
 * giả lập thanh toán bằng cách gọi callback thủ công qua trình duyệt.
 *
 * Cấu hình cần thêm vào application.properties:
 *   vnpay.tmn-code=YOUR_TMN_CODE
 *   vnpay.hash-secret=YOUR_HASH_SECRET
 *   vnpay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
 *   vnpay.return-url=http://localhost:8081/api/payment/vnpay-callback
 */
@Service
public class VNPayService {

    @Value("${vnpay.tmn-code:DEMO1234}")
    private String tmnCode;

    @Value("${vnpay.hash-secret:DEMOSECRETKEY1234567890ABCDEFGH}")
    private String hashSecret;

    @Value("${vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpUrl;

    @Value("${vnpay.return-url:http://localhost:8081/api/payment/vnpay-callback}")
    private String returnUrl;

    /**
     * Tạo URL thanh toán VNPay cho một đơn hàng.
     *
     * @param orderId ID đơn hàng (dùng làm vnp_TxnRef)
     * @param amount  Số tiền cần thanh toán (VNĐ)
     * @return URL thanh toán VNPay
     */
    public String createPaymentUrl(Long orderId, BigDecimal amount) {
        // VNPay yêu cầu amount * 100 (đơn vị xu)
        long vnpAmount = amount.multiply(BigDecimal.valueOf(100)).longValue();

        String vnpTxnRef = String.valueOf(orderId);
        String vnpCreateDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String vnpExpireDate = new SimpleDateFormat("yyyyMMddHHmmss")
                .format(new Date(System.currentTimeMillis() + 15 * 60 * 1000)); // hết hạn sau 15 phút

        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(vnpAmount));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", vnpTxnRef);
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang #" + orderId);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_IpAddr", "127.0.0.1");
        vnpParams.put("vnp_CreateDate", vnpCreateDate);
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);

        // Build query string để hash
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            hashData.append(entry.getKey()).append('=')
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII))
                    .append('&');
            query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII))
                    .append('=')
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII))
                    .append('&');
        }

        // Xóa ký tự '&' cuối cùng
        String hashDataStr = hashData.substring(0, hashData.length() - 1);
        String queryStr    = query.substring(0, query.length() - 1);

        String secureHash = hmacSHA512(hashSecret, hashDataStr);
        String paymentUrl = vnpUrl + "?" + queryStr + "&vnp_SecureHash=" + secureHash;

        return paymentUrl;
    }

    /**
     * HMAC-SHA512 helper - dùng để ký request gửi VNPay.
     */
    private String hmacSHA512(String key, String data) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA512");
            javax.crypto.spec.SecretKeySpec secretKey =
                    new javax.crypto.spec.SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKey);
            byte[] hmacData = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hmacData) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo HMAC-SHA512: " + e.getMessage(), e);
        }
    }
}
