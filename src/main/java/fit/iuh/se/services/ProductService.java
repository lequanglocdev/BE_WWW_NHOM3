package fit.iuh.se.services;

import fit.iuh.se.dtos.ProductDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public interface ProductService {

    ResponseEntity<?> createProductWithImages(
            String name,
            String description,
            BigDecimal price,
            int stock,
            Long categoryId,
            MultipartFile[] files) throws Exception;
    ResponseEntity<?> update(Long id, ProductDTO dto);

    ResponseEntity<?> delete(Long id);

    ResponseEntity<?> getAll();

    ResponseEntity<?> getById(Long id);

    ResponseEntity<?> updateProductWithImages(
            Long id,
            String name,
            String description,
            BigDecimal price,
            Integer stock,
            MultipartFile[] files);
    ResponseEntity<?> search(
            String keyword,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size
    );
}