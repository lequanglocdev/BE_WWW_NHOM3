package fit.iuh.se.controllers;

import fit.iuh.se.dtos.ProductDTO;
import fit.iuh.se.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@RestController
public class ProductController {

    @Autowired
    private ProductService service;

    // 🌍 PUBLIC APIs
    @GetMapping("/products")
    public ResponseEntity<?> getAll() {
        return service.getAll();
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return service.getById(id);
    }

    // 👑 ADMIN APIs

    @PostMapping("/admin/products")
    public ResponseEntity<?> createProduct(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam BigDecimal price,
            @RequestParam int stock,

            @RequestParam Long categoryId,
            @RequestParam("files") MultipartFile[] files
    ) {
        return service.createProductWithImages(name, description, price, stock, categoryId, files);
    }

    @PutMapping("/admin/products/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody ProductDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/admin/products/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return service.delete(id);
    }
}