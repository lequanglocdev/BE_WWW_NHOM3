package fit.iuh.se.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;        // Tên chương trình
    private String code;        // Mã giảm giá (VD: SALE10)

    private String discountType; // PERCENT | AMOUNT

    private Double discountValue;

    private Double minOrderValue; // Đơn tối thiểu để áp mã

    private LocalDate startDate;
    private LocalDate endDate;

    private Boolean isActive = true;

    private Integer usageLimit; // số lượt dùng tối đa
    private Integer usedCount = 0;
}
