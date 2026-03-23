package fit.iuh.se.services;

import fit.iuh.se.dtos.AdminUpdateUserDTO;
import fit.iuh.se.dtos.ApiResponse;
import fit.iuh.se.entities.UserAccount;
import fit.iuh.se.repositories.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    private UserAccountRepository userRepo;

    @Override
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepo.findAll());
    }

    @Override
    public ResponseEntity<?> getUserById(Integer id) {
        UserAccount user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        return ResponseEntity.ok(user);
    }

    @Override
    public ResponseEntity<?> updateUser(Integer id, AdminUpdateUserDTO dto) {
        UserAccount user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        if (dto.getFullName() != null)
            user.setFullName(dto.getFullName().trim());

        if (dto.getPhone() != null)
            user.setPhone(dto.getPhone().trim());

        if (dto.getAddress() != null)
            user.setAddress(dto.getAddress().trim());

        if (dto.getIsActive() != null)
            user.setIsActive(dto.getIsActive());

        if (dto.getIsAdmin() != null)
            user.setIsAdmin(dto.getIsAdmin());

        userRepo.save(user);

        return ResponseEntity.ok(new ApiResponse(true, "Cập nhật user thành công"));
    }

    @Override
    public ResponseEntity<?> deleteUser(Integer id) {
        UserAccount user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        user.setIsActive(false); // ✅ khóa tài khoản
        userRepo.save(user);

        return ResponseEntity.ok(new ApiResponse(true, "Đã khóa user"));
    }
}