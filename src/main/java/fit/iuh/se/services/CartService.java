package fit.iuh.se.services;

import fit.iuh.se.dtos.AddToCartDTO;
import fit.iuh.se.dtos.UpdateCartItemDTO;
import fit.iuh.se.entities.Cart;
import fit.iuh.se.entities.CartItem;
import fit.iuh.se.entities.Product;
import fit.iuh.se.entities.UserAccount;
import fit.iuh.se.repositories.CartItemRepository;
import fit.iuh.se.repositories.CartRepository;
import fit.iuh.se.repositories.ProductRepository;
import fit.iuh.se.repositories.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepo;
    @Autowired private CartItemRepository itemRepo;
    @Autowired private ProductRepository productRepo;
    @Autowired private UserAccountRepository userRepo;

    private Cart getOrCreateCart(UserAccount user) {
        return cartRepo.findByUser(user).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepo.save(cart);
        });
    }

    public ResponseEntity<?> getCart(String email) {
        UserAccount user = userRepo.findByEmail(email).orElseThrow();
        Cart cart = getOrCreateCart(user);
        return ResponseEntity.ok(cart);
    }

    public ResponseEntity<?> addToCart(String email, AddToCartDTO dto) {
        UserAccount user = userRepo.findByEmail(email).orElseThrow();
        Cart cart = getOrCreateCart(user);
        Product product = productRepo.findById(dto.getProductId()).orElseThrow();

        for (CartItem item : cart.getItems()) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + dto.getQuantity());
                itemRepo.save(item);
                return ResponseEntity.ok("Đã tăng số lượng sản phẩm trong giỏ");
            }
        }

        CartItem newItem = new CartItem();
        newItem.setCart(cart);
        newItem.setProduct(product);
        newItem.setQuantity(dto.getQuantity());
        itemRepo.save(newItem);

        return ResponseEntity.ok("Đã thêm sản phẩm vào giỏ");
    }

    public ResponseEntity<?> removeItem(Long itemId) {
        itemRepo.deleteById(itemId);
        return ResponseEntity.ok("Đã xoá sản phẩm khỏi giỏ");
    }

    public ResponseEntity<?> clearCart(String email) {
        UserAccount user = userRepo.findByEmail(email).orElseThrow();
        Cart cart = getOrCreateCart(user);
        cart.getItems().clear();
        cartRepo.save(cart);
        return ResponseEntity.ok("Đã xoá toàn bộ giỏ hàng");
    }

    public ResponseEntity<?> updateQuantity(String email, UpdateCartItemDTO dto) {
        UserAccount user = userRepo.findByEmail(email).orElseThrow();

        CartItem item = itemRepo.findById(dto.getItemId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ"));

        if (!item.getCart().getUser().getId().equals(user.getId()))
            throw new RuntimeException("Không có quyền sửa giỏ hàng");

        if (dto.getQuantity() <= 0)
            throw new RuntimeException("Số lượng phải lớn hơn 0");

        item.setQuantity(dto.getQuantity());
        itemRepo.save(item);

        return ResponseEntity.ok("Cập nhật số lượng thành công");
    }
}