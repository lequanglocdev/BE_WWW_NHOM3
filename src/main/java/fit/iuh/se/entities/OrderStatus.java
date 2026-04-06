package fit.iuh.se.entities;

public enum OrderStatus {
    PENDING,    // Chờ xác nhận (COD)
    UNPAID,     // Chờ thanh toán (VNPay)
    PAID,       // Đã thanh toán
    CANCELLED   // Đã hủy
}
