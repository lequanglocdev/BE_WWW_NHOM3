package fit.iuh.se.controllers;

import fit.iuh.se.dtos.LoginRequestDTO;
import fit.iuh.se.dtos.RegisterRequest;
import fit.iuh.se.dtos.ResetRequest;
import fit.iuh.se.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService auth;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO r) {
        return auth.login(r);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest r) {
        return auth.register(r);
    }


    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam String token) {
        return auth.verifyEmail(token);
    }


    @PostMapping("/forgot")
    public ResponseEntity<?> forgot(@RequestBody Map<String, String> body) {
        return auth.forgotPassword(body.get("email"));
    }

    @PostMapping("/reset")
    public ResponseEntity<?> reset(@RequestBody ResetRequest body) {
        return auth.resetPassword(
                body.getToken(),
                body.getPassword()
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        return auth.refresh(body.get("refreshToken"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication) {
        return auth.logout(authentication.getName());
    }


}
