package fit.iuh.se.dtos;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDTO {
    private String name;
    private String description;
    private BigDecimal price;
    private int stock;
    private Long categoryId; // ⭐ bắt buộc có
}