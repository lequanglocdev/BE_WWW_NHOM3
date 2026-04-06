package fit.iuh.se.services;

import fit.iuh.se.dtos.ChatMessageDTO;
import fit.iuh.se.entities.ChatMessage;
import fit.iuh.se.entities.UserAccount;
import fit.iuh.se.repositories.ChatMessageRepository;
import fit.iuh.se.repositories.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatRepo;
    private final UserAccountRepository userRepo;

    /**
     * Lưu tin nhắn vào DB và trả về DTO để gửi qua WebSocket.
     *
     * @param senderEmail email của người gửi (lấy từ JWT Principal)
     * @param dto         DTO chứa receiverId và content
     * @return DTO đã được điền đầy đủ thông tin
     */
    @Transactional
    public ChatMessageDTO saveAndMapToDTO(String senderEmail, ChatMessageDTO dto) {
        UserAccount sender = userRepo.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người gửi: " + senderEmail));

        UserAccount receiver = userRepo.findById(dto.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người nhận: " + dto.getReceiverId()));

        ChatMessage msg = ChatMessage.builder()
                .content(dto.getContent())
                .sender(sender)
                .receiver(receiver)
                .isRead(false)
                .build();

        ChatMessage saved = chatRepo.save(msg);
        return toDTO(saved);
    }

    /**
     * Lấy toàn bộ lịch sử hội thoại giữa user hiện tại và otherUserId.
     */
    public List<ChatMessageDTO> getHistory(Integer currentUserId, Integer otherUserId) {
        return chatRepo.findConversation(currentUserId, otherUserId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private ChatMessageDTO toDTO(ChatMessage msg) {
        return ChatMessageDTO.builder()
                .id(msg.getId())
                .senderId(msg.getSender().getId())
                .senderName(msg.getSender().getFullName())
                .receiverId(msg.getReceiver().getId())
                .receiverName(msg.getReceiver().getFullName())
                .content(msg.getContent())
                .timestamp(msg.getTimestamp())
                .isRead(msg.getIsRead())
                .build();
    }
}
