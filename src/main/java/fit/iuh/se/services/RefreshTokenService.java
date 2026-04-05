package fit.iuh.se.services;

import fit.iuh.se.entities.RefreshToken;
import fit.iuh.se.entities.UserAccount;
import fit.iuh.se.repositories.RefreshTokenRepository;
import fit.iuh.se.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepo;

    @Autowired
    private JwtUtils jwtUtils;

    // Tạo refresh token mới cho user (xóa token cũ nếu có)
    public RefreshToken createRefreshToken(UserAccount user) {
        // Xóa token cũ nếu tồn tại
        refreshTokenRepo.findByUser(user)
                .ifPresent(refreshTokenRepo::delete);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(jwtUtils.generateRefreshToken(user.getEmail()));
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshToken.setIsRevoked(false);

        return refreshTokenRepo.save(refreshToken);
    }

    // Kiểm tra token còn hợp lệ không
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token không tồn tại"));

        if (refreshToken.getIsRevoked())
            throw new RuntimeException("Refresh token đã bị thu hồi");

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepo.delete(refreshToken); // xóa token hết hạn
            throw new RuntimeException("Refresh token đã hết hạn, vui lòng đăng nhập lại");
        }

        return refreshToken;
    }

    // Thu hồi token khi logout
    public void revokeToken(UserAccount user) {
        refreshTokenRepo.findByUser(user).ifPresent(t -> {
            t.setIsRevoked(true);
            refreshTokenRepo.save(t);
        });
    }
}