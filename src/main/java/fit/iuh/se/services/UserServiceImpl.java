package fit.iuh.se.services;

import fit.iuh.se.dtos.ApiResponse;
import fit.iuh.se.dtos.UpdateUserDTO;
import fit.iuh.se.entities.UserAccount;
import fit.iuh.se.repositories.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserAccountRepository userRepo;

    @Override
    public ResponseEntity<?> getProfile(String email) {
        UserAccount user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        return ResponseEntity.ok(user);
    }

    @Override
    public ResponseEntity<?> updateProfile(String email, UpdateUserDTO dto) {
        UserAccount user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        if (dto.getFullName() != null)
            user.setFullName(dto.getFullName().trim());

        if (dto.getPhone() != null)
            user.setPhone(dto.getPhone().trim());

        if (dto.getAddress() != null)
            user.setAddress(dto.getAddress().trim());

        userRepo.save(user);

        return ResponseEntity.ok(
                new ApiResponse(true, "Cập nhật thông tin thành công")
        );
    }
}