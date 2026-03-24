package fit.iuh.se.controllers;

import fit.iuh.se.dtos.AddToCartDTO;
import fit.iuh.se.dtos.UpdateCartItemDTO;
import fit.iuh.se.services.CartService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/cart")
public class CartController {

    @Autowired
    private CartService service;

    @GetMapping
    public ResponseEntity<?> getCart(Authentication auth) {
        return service.getCart(auth.getName());
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody AddToCartDTO dto,
                                 Authentication auth) {
        return service.addToCart(auth.getName(), dto);
    }

    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<?> remove(@PathVariable Long itemId) {
        return service.removeItem(itemId);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clear(Authentication auth) {
        return service.clearCart(auth.getName());
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody UpdateCartItemDTO dto,
                                    Authentication auth) {
        return service.updateQuantity(auth.getName(), dto);
    }
}