package com.Globoo.chat.repository;

import com.Globoo.chat.domain.ChatMessage;
import com.Globoo.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop100ByRoomOrderByIdDesc(ChatRoom room);
}
