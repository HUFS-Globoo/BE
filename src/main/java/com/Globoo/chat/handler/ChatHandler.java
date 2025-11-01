package com.Globoo.chat.handler;

import com.Globoo.chat.dto.ChatMessageSendReqDto;
import com.Globoo.chat.dto.ChatMessageSendResDto;
import com.Globoo.chat.service.ChatService;
import com.Globoo.common.error.ErrorCode;
import com.Globoo.common.error.exception.EntityNotFoundException;
import com.Globoo.user.domain.User;
import com.Globoo.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ChatService chatService;
    private final UserRepository userRepository;

    private final Map<Long, Set<WebSocketSession>> chatRooms = new HashMap<>();
    private final Map<WebSocketSession, Long> sessionToRoomId = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Principal principal = session.getPrincipal();

        if (principal == null || principal.getName() == null) {
            log.warn("인증되지 않은 사용자가 WebSocket에 연결을 시도했습니다.");
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        log.info("[WebSocket] 연결 성공. 세션 ID: {}, 사용자 ID: {}", session.getId(), principal.getName());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            log.debug("수신 메시지: {}", payload);

            Principal principal = session.getPrincipal();
            if (principal == null) {
                session.close(CloseStatus.POLICY_VIOLATION);
                return;
            }
            User sender = userRepository.findById(Long.parseLong(principal.getName()))
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

            ChatMessageSendReqDto chatDto = objectMapper.readValue(payload, ChatMessageSendReqDto.class);
            Long roomId = chatDto.getRoomId();

            if (!chatRooms.containsKey(roomId)) {
                chatRooms.put(roomId, new HashSet<>());
            }
            Set<WebSocketSession> roomSessions = chatRooms.get(roomId);
            if (!roomSessions.contains(session)) {
                roomSessions.add(session);
                sessionToRoomId.put(session, roomId);
                log.info("[ChatRoom] 세션 {}가 {}번 방에 입장했습니다.", session.getId(), roomId);
            }

            ChatMessageSendResDto resDto = chatService.saveMessageAndCreateDto(chatDto, sender);

            TextMessage textMessage = new TextMessage(objectMapper.writeValueAsString(resDto));
            for (WebSocketSession s : roomSessions) {
                s.sendMessage(textMessage);
            }

        } catch (Exception e) {
            log.error("[handleTextMessage] 메시지 처리 중 오류 발생. 세션 ID: {}", session.getId(), e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("[WebSocket] 연결 종료. 세션 ID: {}, 상태: {}", session.getId(), status);

        Long roomId = sessionToRoomId.get(session);
        if (roomId != null) {
            Set<WebSocketSession> roomSessions = chatRooms.get(roomId);
            if (roomSessions != null) {
                roomSessions.remove(session);
            }
        }
        sessionToRoomId.remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("[WebSocket] 전송 오류. 세션 ID: " + session.getId(), exception);
    }
}