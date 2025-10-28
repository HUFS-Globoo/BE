package com.Globoo.chat.repository;

import com.Globoo.chat.domain.ChatParticipant;
import com.Globoo.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    boolean existsByRoomAndUserId(ChatRoom room, Long userId);
}
