package fit.iuh.se.controllers;
import fit.iuh.se.dtos.CategoryDTO;
import fit.iuh.se.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class CategoryController {

    @Autowired
    private CategoryService service;

    // 🌍 Public
    @GetMapping("/categories")
    public ResponseEntity<?> getAll() {
        return service.getAll();
    }

    // 👑 Admin
    @PostMapping("/admin/categories")
    public ResponseEntity<?> create(@Valid @RequestBody CategoryDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/admin/categories/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody CategoryDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/admin/categories/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return service.delete(id);
    }
}