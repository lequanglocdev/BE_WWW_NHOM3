package fit.iuh.se.services;

import fit.iuh.se.dtos.ApiResponse;
import fit.iuh.se.dtos.LoginRequestDTO;
import fit.iuh.se.dtos.RegisterRequest;
import fit.iuh.se.entities.PasswordResetToken;
import fit.iuh.se.entities.RefreshToken;
import fit.iuh.se.entities.UserAccount;
import fit.iuh.se.entities.VerificationToken;
import fit.iuh.se.repositories.PasswordResetTokenRepository;
import fit.iuh.se.repositories.UserAccountRepository;
import fit.iuh.se.repositories.VerificationTokenRepository;
import fit.iuh.se.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired private UserAccountRepository userRepo;
    @Autowired private VerificationTokenRepository verifyRepo;
    @Autowired private PasswordResetTokenRepository resetRepo;
    @Autowired private PasswordEncoder encoder;
    @Autowired private JwtUtils jwt;
    @Autowired private EmailService emailService;
    @Autowired
    private RefreshTokenService refreshTokenService;

    // REGISTER
    public ResponseEntity<ApiResponse> register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        UserAccount u = new UserAccount();
        String email = req.getEmail().trim().toLowerCase();
        String password = req.getPassword().trim();

        u.setEmail(email);
        u.setPassword(encoder.encode(password));
        u.setFullName(req.getFullName().trim());
        u.setPhone(req.getPhone() != null ? req.getPhone().trim() : null);

        userRepo.save(u);

        // Tạo token verify
        VerificationToken vt = new VerificationToken();
        vt.setToken(UUID.randomUUID().toString());
        vt.setUser(u);
        vt.setExpiryDate(LocalDateTime.now().plusHours(24));
        verifyRepo.save(vt);

        emailService.sendVerify(u.getEmail(), vt.getToken());

        return ResponseEntity.ok(new ApiResponse(true, "Đăng ký thành công"));
    }

    // VERIFY EMAIL
    public ResponseEntity<ApiResponse> verifyEmail(String token) {
        VerificationToken vt = verifyRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token sai hoặc không tồn tại"));

        if (vt.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Link đã hết hạn!");
        }

        UserAccount u = vt.getUser();
        u.setIsVerified(true);
        userRepo.save(u);
        verifyRepo.delete(vt);
        return ResponseEntity.ok(new ApiResponse(true, "Xác thực thành công!"));
    }

    // LOGIN
    public ResponseEntity<?> login(LoginRequestDTO req) {
        UserAccount u = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        String rawPassword = req.getPassword().trim();

        if (!encoder.matches(rawPassword, u.getPassword()))
            throw new RuntimeException("Sai mật khẩu");

        if (!u.getIsVerified())
            throw new RuntimeException("Tài khoản chưa được xác thực");

        if (!u.getIsActive())
            throw new RuntimeException("Tài khoản đã bị khóa");

        String token = jwt.generateToken(u.getEmail(), u.getIsAdmin());
        String role = u.getIsAdmin() ? "ADMIN" : "USER";

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(u);
        Map<String, Object> res = new HashMap<>();
        res.put("status", true);
        res.put("message", "Đăng nhập thành công");
        res.put("accessToken", token);
        res.put("refreshToken", refreshToken.getToken()); // ✅ client cần cái này
        res.put("role", role);

        return ResponseEntity.ok(res);
    }

    // FORGOT PASSWORD
    public ResponseEntity<ApiResponse> forgotPassword(String email) {
        UserAccount u = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken(UUID.randomUUID().toString());
        prt.setUser(u);
        prt.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        resetRepo.save(prt);

        emailService.sendReset(email, prt.getToken());

        return ResponseEntity.ok(new ApiResponse(true, "Vui lòng kiểm tra email để đặt lại mật khẩu"));
    }

    // RESET PASSWORD
    public ResponseEntity<ApiResponse> resetPassword(String token, String newPass) {
        PasswordResetToken prt = resetRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));

        if (prt.getExpiryDate().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Token đã hết hạn");

        UserAccount u = prt.getUser();
        if (newPass == null || newPass.trim().isEmpty()) {
            throw new RuntimeException("Password không được để trống");
        }

        String encoded = encoder.encode(newPass.trim());
        u.setPassword(encoded);
        userRepo.save(u);
        resetRepo.delete(prt);
        return ResponseEntity.ok(new ApiResponse(true, "Đổi mật khẩu thành công"));
    }

    public ResponseEntity<?> refresh(String refreshToken) {
        RefreshToken rt = refreshTokenService.verifyRefreshToken(refreshToken);
        UserAccount user = rt.getUser();

        String newAccessToken = jwt.generateToken(user.getEmail(), user.getIsAdmin());

        Map<String, Object> res = new HashMap<>();
        res.put("status", true);
        res.put("accessToken", newAccessToken);

        return ResponseEntity.ok(res);
    }
    public ResponseEntity<?> logout(String email) {
        UserAccount user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        refreshTokenService.revokeToken(user);

        return ResponseEntity.ok(new ApiResponse(true, "Đăng xuất thành công"));
    }
}
