package fit.iuh.se.controllers;

import fit.iuh.se.dtos.AdminUpdateUserDTO;
import fit.iuh.se.services.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    // 👑 Xem toàn bộ user
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return adminUserService.getAllUsers();
    }

    // 👑 Xem chi tiết user
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Integer id) {
        return adminUserService.getUserById(id);
    }

    // 👑 Sửa thông tin user
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Integer id,
            @RequestBody AdminUpdateUserDTO dto) {
        return adminUserService.updateUser(id, dto);
    }

    // 👑 Xóa user
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        return adminUserService.deleteUser(id);
    }
}