package fit.iuh.se.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    private Boolean isActive = true;

    // 1 danh mục có nhiều sản phẩm
    @OneToMany(mappedBy = "category")
    private List<Product> products;
}