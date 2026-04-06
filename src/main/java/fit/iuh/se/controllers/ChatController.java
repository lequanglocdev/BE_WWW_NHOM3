package fit.iuh.se.controllers;

import fit.iuh.se.dtos.ChatMessageDTO;
import fit.iuh.se.entities.UserAccount;
import fit.iuh.se.repositories.UserAccountRepository;
import fit.iuh.se.services.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Controller xử lý WebSocket Chat và REST API lịch sử trò chuyện.
 *
 * ─── WebSocket flow ──────────────────────────────────────────────────────────
 *  1. Client kết nối: ws://localhost:8081/api/ws  (SockJS)
 *  2. STOMP CONNECT với header: Authorization: Bearer <token>
 *  3. Client subscribe: /user/queue/messages
 *  4. Client gửi:      /app/chat.send  với body ChatMessageDTO {receiverId, content}
 *  5. Server lưu DB → gửi đến cả sender lẫn receiver qua /user/{email}/queue/messages
 *
 * ─── REST API ────────────────────────────────────────────────────────────────
 *  GET /api/chat/history/{otherUserId}   → lịch sử hội thoại
 * ─────────────────────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final UserAccountRepository userRepo;

    // ── WebSocket ─────────────────────────────────────────────────────────────

    /**
     * Nhận tin nhắn từ client qua STOMP,
     * lưu vào DB rồi đẩy đến cả sender lẫn receiver.
     *
     * @param dto       payload từ client (receiverId, content)
     * @param principal được Spring inject tự động từ accessor.setUser() trong interceptor
     */
    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessageDTO dto, Principal principal) {
        if (principal == null) {
            log.warn("[Chat] Unauthenticated WebSocket message dropped.");
            return;
        }

        String senderEmail = principal.getName();
        log.info("[Chat] Message from {} → receiverId={}", senderEmail, dto.getReceiverId());

        // 1. Lưu vào DB và lấy DTO đầy đủ
        ChatMessageDTO saved = chatService.saveAndMapToDTO(senderEmail, dto);

        // 2. Lấy email của receiver để route tin nhắn
        String receiverEmail = userRepo.findById(dto.getReceiverId())
                .map(UserAccount::getEmail)
                .orElse(null);

        if (receiverEmail == null) {
            log.warn("[Chat] Receiver {} not found, aborting send.", dto.getReceiverId());
            return;
        }

        // 3. Gửi đến receiver: /user/{receiverEmail}/queue/messages
        messagingTemplate.convertAndSendToUser(receiverEmail, "/queue/messages", saved);

        // 4. Echo lại cho sender để cập nhật UI (gửi thành công)
        messagingTemplate.convertAndSendToUser(senderEmail, "/queue/messages", saved);

        log.info("[Chat] Delivered msgId={} to {} and {}", saved.getId(), receiverEmail, senderEmail);
    }

    // ── REST API ──────────────────────────────────────────────────────────────

    /**
     * Lấy lịch sử hội thoại giữa user hiện tại và user khác.
     * Yêu cầu JWT hợp lệ trong header Authorization.
     *
     * @param otherUserId  ID của người còn lại
     * @param principal    Spring inject từ SecurityContext (JwtFilter)
     */
    @GetMapping("/history/{otherUserId}")
    public ResponseEntity<?> getChatHistory(
            @PathVariable Integer otherUserId,
            @AuthenticationPrincipal(expression = "name") String currentEmail) {

        if (currentEmail == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("status", false, "message", "Bạn chưa đăng nhập"));
        }

        UserAccount currentUser = userRepo.findByEmail(currentEmail).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("status", false, "message", "Không tìm thấy tài khoản"));
        }

        List<ChatMessageDTO> history = chatService.getHistory(currentUser.getId(), otherUserId);
        return ResponseEntity.ok(Map.of("status", true, "data", history));
    }
}
