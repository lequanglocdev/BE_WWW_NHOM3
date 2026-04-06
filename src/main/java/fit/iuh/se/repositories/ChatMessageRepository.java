package fit.iuh.se.repositories;

import fit.iuh.se.entities.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Lấy toàn bộ tin nhắn giữa 2 người dùng, sắp xếp theo thời gian tăng dần.
     */
    @Query("""
            SELECT m FROM ChatMessage m
            WHERE (m.sender.id = :userId AND m.receiver.id = :otherId)
               OR (m.sender.id = :otherId AND m.receiver.id = :userId)
            ORDER BY m.timestamp ASC
            """)
    List<ChatMessage> findConversation(@Param("userId") Integer userId,
                                       @Param("otherId") Integer otherId);
}
