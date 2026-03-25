package fit.iuh.se.controllers;

import fit.iuh.se.services.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/promotions")
public class UserPromotionController {

    @Autowired
    private PromotionService service;

    @PostMapping("/apply")
    public ResponseEntity<?> apply(Authentication auth,
                                   @RequestParam String code) {
        return service.applyPromotion(auth.getName(), code);
    }
}
