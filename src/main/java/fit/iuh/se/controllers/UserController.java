package fit.iuh.se.controllers;

import fit.iuh.se.dtos.UpdateUserDTO;
import fit.iuh.se.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 👤 Xem thông tin của mình
    @GetMapping("/me")
    public ResponseEntity<?> getProfile(Authentication auth) {
        return userService.getProfile(auth.getName());
    }

    // ✏️ Cập nhật thông tin
    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(
            Authentication auth,
            @RequestBody UpdateUserDTO dto) {
        return userService.updateProfile(auth.getName(), dto);
    }
}