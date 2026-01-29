package com.Globoo.chat.service;

import com.Globoo.user.domain.User;
import com.Globoo.user.repository.UserRepository;
import com.Globoo.chat.domain.*;
import com.Globoo.chat.dto.*;
import com.Globoo.chat.repository.*;
import com.Globoo.common.error.ErrorCode;
import com.Globoo.common.error.AuthException;
import com.Globoo.common.error.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatRoomCreateResDto createChatRoom(ChatRoomCreateReqDto dto, Long currentUserId) {
        User currentUser = findUserById(currentUserId);
        User participantUser = findUserById(dto.getParticipantUserId());

        ChatRoom chatRoom = ChatRoom.builder().build();
        chatRoomRepository.save(chatRoom);

        ChatParticipant selfParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .user(currentUser)
                .build();
        ChatParticipant otherParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .user(participantUser)
                .build();

        chatParticipantRepository.saveAll(List.of(selfParticipant, otherParticipant));

        chatRoom.addParticipant(selfParticipant);
        chatRoom.addParticipant(otherParticipant);

        return new ChatRoomCreateResDto(chatRoom.getId());
    }

    @Transactional(readOnly = true)
    public List<ChatRoomGetResDto> findMyChatRooms(Long currentUserId) {
        User currentUser = findUserById(currentUserId);

        List<ChatParticipant> myParticipants = chatParticipantRepository.findByUser(currentUser);

        return myParticipants.stream().map(participant -> {
            ChatRoom chatRoom = participant.getChatRoom();
            User otherUser = findOtherParticipant(chatRoom, currentUser);
            return new ChatRoomGetResDto(chatRoom, otherUser.getUsername()); //Nickname -> Username 수정
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChatMessageGetResDto> findChatRoomMessages(Long roomId, Long currentUserId) {
        User currentUser = findUserById(currentUserId);
        ChatRoom chatRoom = findChatRoomById(roomId);

        checkParticipant(chatRoom, currentUser);

        return chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom).stream()
                .map(ChatMessageGetResDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatMessageSendResDto saveMessageAndCreateDto(ChatMessageSendReqDto dto, User sender) {

        ChatRoom chatRoom = findChatRoomById(dto.getRoomId());

        checkParticipant(chatRoom, sender);

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .message(dto.getMessage())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        chatRoom.updateLastMessage(savedMessage.getMessage(), savedMessage.getCreatedAt());

        return ChatMessageSendResDto.from(savedMessage);
    }

    @Transactional
    public void updateLastReadMessageId(Long readerId, Long roomId, Long lastReadMessageId) {
        User reader = findUserById(readerId);
        ChatRoom chatRoom = findChatRoomById(roomId);

        ChatParticipant participant = chatParticipantRepository.findByChatRoomAndUser(chatRoom, reader)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.NOT_CHAT_PARTICIPANT));

        participant.updateLastReadMessageId(lastReadMessageId);
    }

    /**
     * ✅ 일회성 채팅 종료 시 방 삭제용 (chat_participant/chat_message는 DDL의 ON DELETE CASCADE로 같이 삭제됨)
     */
    @Transactional
    public void deleteRoom(Long roomId) {
        if (roomId == null) return;
        if (!chatRoomRepository.existsById(roomId)) return;
        chatRoomRepository.deleteById(roomId);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    private ChatRoom findChatRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    private void checkParticipant(ChatRoom chatRoom, User user) {
        chatParticipantRepository.findByChatRoomAndUser(chatRoom, user)
                .orElseThrow(() -> new AuthException(ErrorCode.FORBIDDEN_ACCESS));
    }

    private User findOtherParticipant(ChatRoom chatRoom, User currentUser) {
        return chatParticipantRepository.findByChatRoom(chatRoom).stream()
                .map(ChatParticipant::getUser)
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
    }
}
