package fit.iuh.se.services;

import fit.iuh.se.dtos.ApiResponse;
import fit.iuh.se.dtos.ProductDTO;
import fit.iuh.se.entities.Category;
import fit.iuh.se.entities.Product;
import fit.iuh.se.entities.ProductImage;
import fit.iuh.se.repositories.CategoryRepository;
import fit.iuh.se.repositories.ProductImageRepository;
import fit.iuh.se.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.util.Map;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository repo;


    @Autowired
    private ProductImageRepository imageRepo;


    @Autowired
    private CategoryRepository categoryRepo;

    @Autowired
    private Cloudinary cloudinary;


    @Override
    public ResponseEntity<?> createProductWithImages(
            String name,
            String description,
            BigDecimal price,
            int stock,
            Long categoryId,
            MultipartFile[] files) throws  Exception {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        product.setCategory(category);
        repo.save(product);

        for (MultipartFile file : files) {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(), ObjectUtils.emptyMap()
            );
            String imageUrl = uploadResult.get("secure_url").toString();

            ProductImage img = new ProductImage();
            img.setImageUrl(imageUrl);
            img.setProduct(product);
            imageRepo.save(img);
        }
        return ResponseEntity.ok(new ApiResponse(true, "Tạo sản phẩm kèm ảnh thành công"));
    }

    @Override
    public ResponseEntity<?> update(Long id, ProductDTO dto) {
        Product p = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (dto.getName() != null) p.setName(dto.getName());
        if (dto.getDescription() != null) p.setDescription(dto.getDescription());
        if (dto.getPrice() != null && dto.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            p.setPrice(dto.getPrice());
        }
        if (dto.getStock() >= 0) p.setStock(dto.getStock());


        repo.save(p);
        return ResponseEntity.ok(new ApiResponse(true, "Cập nhật sản phẩm thành công"));
    }

    @Override
    public ResponseEntity<?> delete(Long id) {
        Product p = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        p.setIsActive(false); // soft delete
        repo.save(p);

        return ResponseEntity.ok(new ApiResponse(true, "Đã xóa sản phẩm"));
    }

    @Override
    public ResponseEntity<?> getAll() {
//        return ResponseEntity.ok(repo.findAll());
        return ResponseEntity.ok(repo.findByIsActiveTrue()); // ✅
    }

    @Override
    public ResponseEntity<?> getById(Long id) {
        return ResponseEntity.ok(
                repo.findById(id)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"))
        );
    }

    @Override
    public ResponseEntity<?> updateProductWithImages(
            Long id,
            String name,
            String description,
            BigDecimal price,
            Integer stock,
            MultipartFile[] files) {

        try {
            Product p = repo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            if (name != null) p.setName(name);
            if (description != null) p.setDescription(description);
            if (price != null && price.compareTo(BigDecimal.ZERO) > 0) p.setPrice(price);
            if (stock != null && stock >= 0) p.setStock(stock);

            repo.save(p);

            // Nếu có upload ảnh mới
            if (files != null && files.length > 0) {
                for (MultipartFile file : files) {

                    Map uploadResult = cloudinary.uploader().upload(
                            file.getBytes(),
                            ObjectUtils.asMap("folder", "ecommerce/products")
                    );

                    String imageUrl = uploadResult.get("secure_url").toString();

                    ProductImage img = new ProductImage();
                    img.setImageUrl(imageUrl);
                    img.setProduct(p);

                    imageRepo.save(img);
                }
            }

            return ResponseEntity.ok(new ApiResponse(true, "Cập nhật sản phẩm + ảnh thành công"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi update: " + e.getMessage());
        }
    }
}