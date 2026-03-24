package fit.iuh.se.dtos;

import lombok.Data;

@Data
public class AddToCartDTO {
    private Long productId;
    private int quantity;
}