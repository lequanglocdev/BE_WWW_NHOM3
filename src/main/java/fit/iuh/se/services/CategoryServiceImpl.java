package fit.iuh.se.services;

import fit.iuh.se.dtos.CategoryDTO;
import fit.iuh.se.entities.Category;
import fit.iuh.se.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository repo;

    @Override
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(repo.findAll());
    }

    @Override
    public ResponseEntity<?> create(CategoryDTO dto) {
        Category c = new Category();
        c.setName(dto.getName());
        c.setDescription(dto.getDescription());
        repo.save(c);
        return ResponseEntity.ok("Thêm danh mục thành công");
    }

    @Override
    public ResponseEntity<?> update(Long id, CategoryDTO dto) {
        Category c = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        if (dto.getName() != null) c.setName(dto.getName());
        if (dto.getDescription() != null) c.setDescription(dto.getDescription());

        repo.save(c);
        return ResponseEntity.ok("Cập nhật danh mục thành công");
    }

    @Override
    public ResponseEntity<?> delete(Long id) {
        Category c = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        c.setIsActive(false); // soft delete
        repo.save(c);
        return ResponseEntity.ok("Đã xóa danh mục");
    }
}