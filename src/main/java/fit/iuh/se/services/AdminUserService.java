package fit.iuh.se.services;
import fit.iuh.se.dtos.AdminUpdateUserDTO;
import org.springframework.http.ResponseEntity;

public interface AdminUserService {
    ResponseEntity<?> getAllUsers();
    ResponseEntity<?> getUserById(Integer id);
    ResponseEntity<?> updateUser(Integer id, AdminUpdateUserDTO dto);
    ResponseEntity<?> deleteUser(Integer id);
}
