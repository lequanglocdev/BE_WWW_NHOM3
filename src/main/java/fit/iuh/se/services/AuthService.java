package fit.iuh.se.services;

import fit.iuh.se.dtos.LoginRequestDTO;
import fit.iuh.se.dtos.RegisterRequest;
import org.springframework.http.ResponseEntity;

public interface  AuthService {
    ResponseEntity<?> register(RegisterRequest req);
    ResponseEntity<?> login(LoginRequestDTO req);
    ResponseEntity<?>  verifyEmail(String token);
    ResponseEntity<?> forgotPassword(String email);
    ResponseEntity<?> resetPassword(String token, String newPass);
}
