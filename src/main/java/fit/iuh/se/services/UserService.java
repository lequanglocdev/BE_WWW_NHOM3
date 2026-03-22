package fit.iuh.se.services;

import fit.iuh.se.dtos.UpdateUserDTO;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<?> getProfile(String email);
    ResponseEntity<?> updateProfile(String email, UpdateUserDTO dto);
}
