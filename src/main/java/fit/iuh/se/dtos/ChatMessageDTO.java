package fit.iuh.se.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO dùng để serialize/deserialize tin nhắn qua WebSocket và REST API.
 * Tránh vòng lặp JSON từ quan hệ Entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {

    private Long id;

    /** ID người nhận — client gửi lên để server biết route đến ai */
    private Integer receiverId;

    /** ID người gửi — server tự điền từ JWT, client không tự đặt được */
    private Integer senderId;

    private String senderName;
    private String receiverName;

    private String content;

    private LocalDateTime timestamp;

    private Boolean isRead;
}
