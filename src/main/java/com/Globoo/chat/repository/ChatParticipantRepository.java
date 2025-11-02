package com.Globoo.chat.repository;

import com.Globoo.user.domain.User;
import com.Globoo.chat.domain.ChatParticipant;
import com.Globoo.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    // 사용자가 참여하고 있는 모든 채팅방 조회
    List<ChatParticipant> findByUser(User user);

    // 특정 채팅방에 특정 사용자가 참여했는지 확인
    Optional<ChatParticipant> findByChatRoomAndUser(ChatRoom chatRoom, User user);

    // 특정 채팅방의 모든 참여자 조회
    List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);
}