package fit.iuh.se.config;

import fit.iuh.se.entities.UserAccount;
import fit.iuh.se.repositories.UserAccountRepository;
import fit.iuh.se.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Interceptor xác thực JWT khi client kết nối WebSocket qua STOMP CONNECT frame.
 *
 * Client gửi token trong STOMP header:
 *   Authorization: Bearer <token>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final UserAccountRepository userRepo;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        // Chỉ xử lý frame CONNECT
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            log.debug("[WS] CONNECT received, Authorization header: {}", authHeader);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    if (jwtUtils.validateToken(token)) {
                        String email = jwtUtils.extractEmail(token);
                        UserAccount user = userRepo.findByEmail(email).orElse(null);

                        if (user != null && Boolean.TRUE.equals(user.getIsActive())) {
                            String role = Boolean.TRUE.equals(user.getIsAdmin())
                                    ? "ROLE_ADMIN" : "ROLE_USER";

                            UsernamePasswordAuthenticationToken auth =
                                    new UsernamePasswordAuthenticationToken(
                                            user.getEmail(),
                                            null,
                                            List.of(new SimpleGrantedAuthority(role))
                                    );
                            // Đặt Principal vào session WebSocket
                            // → convertAndSendToUser(email, ...) sẽ hoạt động chính xác
                            accessor.setUser(auth);
                            log.info("[WS] Authenticated user: {}", email);
                        }
                    }
                } catch (Exception e) {
                    log.warn("[WS] JWT validation failed: {}", e.getMessage());
                }
            }
        }

        return message;
    }
}
