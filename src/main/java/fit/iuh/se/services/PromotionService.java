package fit.iuh.se.services;

import fit.iuh.se.dtos.ApiResponse;
import fit.iuh.se.entities.Cart;
import fit.iuh.se.entities.Promotion;
import fit.iuh.se.entities.UserAccount;
import fit.iuh.se.repositories.CartRepository;
import fit.iuh.se.repositories.PromotionRepository;
import fit.iuh.se.repositories.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class PromotionService {

    @Autowired
    private PromotionRepository promoRepo;
    @Autowired
    private CartRepository cartRepo;
    @Autowired
    private UserAccountRepository userRepo;

    // =============================
    // ADMIN CRUD
    // =============================

    public ResponseEntity<?> create(Promotion p) {
        p.setIsActive(true);
        return ResponseEntity.ok(promoRepo.save(p));
    }

    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(promoRepo.findAll());
    }

    public ResponseEntity<?> getById(Long id) {
        return ResponseEntity.ok(
                promoRepo.findById(id)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy mã KM"))
        );
    }

    public ResponseEntity<?> update(Long id, Promotion p) {
        Promotion old = promoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã KM"));

        old.setName(p.getName());
        old.setCode(p.getCode());
        old.setDiscountType(p.getDiscountType());
        old.setDiscountValue(p.getDiscountValue());
        old.setStartDate(p.getStartDate());
        old.setEndDate(p.getEndDate());

        return ResponseEntity.ok(promoRepo.save(old));
    }

    public ResponseEntity<?> delete(Long id) {
        promoRepo.deleteById(id);
        return ResponseEntity.ok(new ApiResponse(true,"Đã xóa mã khuyến mãi"));
    }

    public ResponseEntity<?> disable(Long id) {
        Promotion p = promoRepo.findById(id).orElseThrow();
        p.setIsActive(false);
        promoRepo.save(p);
        return ResponseEntity.ok("Đã tắt mã");
    }

    public ResponseEntity<?> enable(Long id) {
        Promotion p = promoRepo.findById(id).orElseThrow();
        p.setIsActive(true);
        promoRepo.save(p);
        return ResponseEntity.ok( new ApiResponse(true,"Đã bật mã"));
    }

    // =============================
    // USER APPLY PROMOTION
    // =============================

    public ResponseEntity<?> applyPromotion(String email, String code) {
        UserAccount user = userRepo.findByEmail(email).orElseThrow();
        Cart cart = cartRepo.findByUser(user).orElseThrow();

        Promotion promo = promoRepo.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Mã không tồn tại"));

        // ❌ Check active
        if (!promo.getIsActive())
            throw new RuntimeException("Mã đã bị vô hiệu");

        // ❌ Check date
        LocalDate today = LocalDate.now();
        if (today.isBefore(promo.getStartDate()) || today.isAfter(promo.getEndDate()))
            throw new RuntimeException("Mã đã hết hạn");

        // ❌ Check cart rỗng
        if (cart.getItems().isEmpty())
            throw new RuntimeException("Giỏ hàng trống");

        // ✅ Tính tổng tiền
        BigDecimal total = cart.getItems().stream()
                .map(i -> i.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = calculateDiscount(total, promo);
        BigDecimal finalTotal = total.subtract(discount);

        Map<String, Object> res = new HashMap<>();
        res.put("status", true);
        res.put("originalTotal", total);
        res.put("discount", discount);
        res.put("finalTotal", finalTotal);
        res.put("promotionCode", promo.getCode());

        return ResponseEntity.ok(res);
    }

    // =============================
    // DISCOUNT CALCULATOR
    // =============================

    private BigDecimal calculateDiscount(BigDecimal total, Promotion promo) {
        if (promo.getDiscountType().equalsIgnoreCase("PERCENT")) {
            return total.multiply(BigDecimal.valueOf(promo.getDiscountValue()))
                    .divide(BigDecimal.valueOf(100));
        } else { // FIXED
            return BigDecimal.valueOf(promo.getDiscountValue());
        }
    }
}
