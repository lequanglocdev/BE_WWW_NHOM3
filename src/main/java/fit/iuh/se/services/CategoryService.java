package fit.iuh.se.services;
import fit.iuh.se.dtos.CategoryDTO;
import org.springframework.http.ResponseEntity;
public interface CategoryService {
    ResponseEntity<?> getAll();
    ResponseEntity<?> create(CategoryDTO dto);
    ResponseEntity<?> update(Long id, CategoryDTO dto);
    ResponseEntity<?> delete(Long id);
}