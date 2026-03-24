package fit.iuh.se.dtos;

import lombok.Data;

@Data
public class UpdateCartItemDTO {
    private Long itemId;
    private int quantity;
}