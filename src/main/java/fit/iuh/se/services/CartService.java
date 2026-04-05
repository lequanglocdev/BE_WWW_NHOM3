package fit.iuh.se.services;

import fit.iuh.se.dtos.AddToCartDTO;
import fit.iuh.se.dtos.ApiResponse;
import fit.iuh.se.dtos.UpdateCartItemDTO;
import fit.iuh.se.entities.*;
import fit.iuh.se.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepo;
    @Autowired private CartItemRepository itemRepo;
    @Autowired private ProductRepository productRepo;
    @Autowired private UserAccountRepository userRepo;

    @Autowired private PromotionRepository promoRepo;

    private Cart getOrCreateCart(UserAccount user) {
        return cartRepo.findByUser(user).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepo.save(cart);
        });
    }

    public ResponseEntity<?> getCart(String email) {
        UserAccount user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        Cart cart = getOrCreateCart(user);
        return ResponseEntity.ok(cart);
    }

    public ResponseEntity<?> addToCart(String email, AddToCartDTO dto) {
        UserAccount user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        ;
        Cart cart = getOrCreateCart(user);
        Product product = productRepo.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));;

        for (CartItem item : cart.getItems()) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + dto.getQuantity());
                itemRepo.save(item);
                return ResponseEntity.ok(new ApiResponse(true, "Đã tăng số lượng sản phẩm trong giỏ"));
            }
        }

        CartItem newItem = new CartItem();
        newItem.setCart(cart);
        newItem.setProduct(product);
        newItem.setQuantity(dto.getQuantity());
        itemRepo.save(newItem);

        return ResponseEntity.ok(new ApiResponse(true, "Đã thêm sản phẩm vào giỏ"));
    }

    public ResponseEntity<?> removeItem(Long itemId) {
        itemRepo.deleteById(itemId);
        return ResponseEntity.ok(new ApiResponse(true, "Đã xoá sản phẩm khỏi giỏ"));
    }

    public ResponseEntity<?> clearCart(String email) {
        UserAccount user =userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));;
        Cart cart = getOrCreateCart(user);
        cart.getItems().clear();
        cartRepo.save(cart);
        return ResponseEntity.ok(new ApiResponse(true, "Đã xoá toàn bộ giỏ hàng"));
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

        return ResponseEntity.ok(new ApiResponse(true, "Cập nhật số lượng thành công"));
    }
    public ResponseEntity<?> applyPromo(String email, String code){

        UserAccount user = userRepo.findByEmail(email).orElseThrow();
        Cart cart =cartRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng"));;

        if (cart.getItems().isEmpty())                              // ✅ thêm vào đây
            throw new RuntimeException("Giỏ hàng đang trống");
        Promotion promo = promoRepo.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Mã không tồn tại"));

        if(!promo.getIsActive())
            throw new RuntimeException("Mã đã bị khóa");

        LocalDate today = LocalDate.now();
        if(today.isBefore(promo.getStartDate()) || today.isAfter(promo.getEndDate()))
            throw new RuntimeException("Mã hết hạn");

        double total = cart.getItems().stream()
                .mapToDouble(i -> i.getProduct().getPrice().doubleValue() * i.getQuantity())
                .sum();

        if(total < promo.getMinOrderValue())
            throw new RuntimeException("Đơn chưa đủ điều kiện");

        double discount = 0;

        if(promo.getDiscountType().equals("PERCENT"))
            discount = total * promo.getDiscountValue() / 100;
        else
            discount = promo.getDiscountValue();

        cart.setPromotion(promo);
        cart.setDiscountAmount(discount);
        cart.setFinalAmount(total - discount);

        cartRepo.save(cart);

        return ResponseEntity.ok(new ApiResponse(true, "Áp mã thành công. Giảm: " + discount));
    }

}