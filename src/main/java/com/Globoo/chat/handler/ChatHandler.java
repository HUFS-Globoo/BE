package com.Globoo.chat.handler;

import com.Globoo.chat.dto.*;
import com.Globoo.chat.service.ChatService;
import com.Globoo.common.error.EntityNotFoundException;
import com.Globoo.common.error.ErrorCode;
import com.Globoo.chat.event.ChatSessionEndedEvent;
import com.Globoo.matching.service.MatchingService;
import com.Globoo.user.domain.User;
import com.Globoo.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ChatService chatService;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    private final Map<Long, Set<WebSocketSession>> chatRooms = new ConcurrentHashMap<>();
    private final Map<WebSocketSession, Long> sessionToRoomId = new ConcurrentHashMap<>();
    private final Map<WebSocketSession, Long> sessionToUserId = new ConcurrentHashMap<>();

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

            JsonNode jsonNode = objectMapper.readTree(payload);
            String type = jsonNode.has("type") ? jsonNode.get("type").asText() : null;
            Long roomId = getRoomIdFromJson(jsonNode);

            if (type == null || roomId == null) {
                if ("JOIN".equals(type) && jsonNode.has("chatRoomId")) {
                    roomId = jsonNode.get("chatRoomId").asLong();
                } else if ("JOIN".equals(type) && jsonNode.has("roomId")) {
                    roomId = jsonNode.get("roomId").asLong();
                } else {
                    log.warn("type 또는 roomId/chatRoomId가 없는 메시지 수신: {}", payload);
                    return;
                }
            }

            BaseWebSocketMessageDto baseDto = objectMapper.readValue(payload, BaseWebSocketMessageDto.class);
            User currentUser = getCurrentUser(session);
            if (currentUser == null) return;

            if (baseDto instanceof ChatRoomJoinReqDto) {
                ((ChatRoomJoinReqDto) baseDto).setRoomId(roomId);
            } else if (baseDto instanceof ChatMessageSendReqDto) {
                ((ChatMessageSendReqDto) baseDto).setRoomId(roomId);
            } else if (baseDto instanceof ReadMessageReqDto) {
                ((ReadMessageReqDto) baseDto).setRoomId(roomId);
            } else if (baseDto instanceof LeaveRoomReqDto) {
                ((LeaveRoomReqDto) baseDto).setRoomId(roomId);
            }

            switch (type) {
                case "JOIN":
                    handleJoinMessage(session, (ChatRoomJoinReqDto) baseDto, currentUser);
                    break;
                case "MESSAGE":
                    handleChatMessage(session, (ChatMessageSendReqDto) baseDto, currentUser);
                    break;
                case "READ":
                    handleReadMessage(session, (ReadMessageReqDto) baseDto, currentUser);
                    break;
                case "LEAVE":
                    handleLeaveMessage(session, (LeaveRoomReqDto) baseDto, currentUser);
                    break;
                default:
                    log.warn("알 수 없는 메시지 타입 수신: {}", type);
            }

        } catch (Exception e) {
            log.error("[handleTextMessage] 메시지 처리 중 오류 발생. 세션 ID: {}", session.getId(), e);
        }
    }

    private Long getRoomIdFromJson(JsonNode jsonNode) {
        if (jsonNode.has("roomId")) {
            return jsonNode.get("roomId").asLong();
        } else if (jsonNode.has("chatRoomId")) {
            return jsonNode.get("chatRoomId").asLong();
        }
        return null;
    }

    private void handleJoinMessage(WebSocketSession session, ChatRoomJoinReqDto joinDto, User user) {
        Long roomId = joinDto.getRoomId();
        enterRoomIfNeeded(session, roomId, user);
        log.info("[JOIN] 사용자 {}가 {}번 방에 입장했습니다.", user.getId(), roomId);
    }

    private void handleChatMessage(WebSocketSession session, ChatMessageSendReqDto chatDto, User sender) throws Exception {
        Long roomId = chatDto.getRoomId();

        if (!chatRooms.getOrDefault(roomId, Set.of()).contains(session)) {
            log.warn("[MESSAGE] 사용자가 {}번 방에 JOIN하지 않고 메시지를 보냈습니다. (세션 ID: {})", roomId, session.getId());
            enterRoomIfNeeded(session, roomId, sender);
        }

        ChatMessageSendResDto resDto = chatService.saveMessageAndCreateDto(chatDto, sender);
        broadcastMessage(roomId, objectMapper.writeValueAsString(resDto));
    }

    private void handleReadMessage(WebSocketSession session, ReadMessageReqDto readDto, User reader) throws Exception {
        Long roomId = readDto.getRoomId();
        Long lastReadMessageId = readDto.getLastReadMessageId();
        log.info("[Read] 사용자 {}가 {}번 방 메시지를 {}번까지 읽음", reader.getId(), roomId, lastReadMessageId);

        if (!chatRooms.getOrDefault(roomId, Set.of()).contains(session)) {
            log.warn("[READ] 사용자가 {}번 방에 JOIN하지 않고 읽음 처리를 보냈습니다. (세션 ID: {})", roomId, session.getId());
            enterRoomIfNeeded(session, roomId, reader);
        }

        ReadReceiptResDto readReceiptDto = new ReadReceiptResDto(reader.getId(), roomId, lastReadMessageId);
        String jsonResponse = objectMapper.writeValueAsString(readReceiptDto);
        Set<WebSocketSession> roomSessions = chatRooms.get(roomId);
        if (roomSessions != null) {
            for (WebSocketSession targetSession : roomSessions) {
                if (!targetSession.getId().equals(session.getId())) {
                    sendMessage(targetSession, jsonResponse);
                }
            }
        }
        chatService.updateLastReadMessageId(reader.getId(), roomId, lastReadMessageId);
    }

    private void handleLeaveMessage(WebSocketSession session, LeaveRoomReqDto leaveDto, User leaver) throws Exception {
        Long roomId = leaveDto.getRoomId();
        log.info("[Leave] 사용자 {}가 {}번 방에서 나감", leaver.getId(), roomId);
        LeaveRoomResDto leaveNoticeDto = new LeaveRoomResDto(leaver.getId(), roomId);
        String jsonResponse = objectMapper.writeValueAsString(leaveNoticeDto);
        Set<WebSocketSession> roomSessions = chatRooms.get(roomId);
        if (roomSessions != null) {
            for (WebSocketSession targetSession : roomSessions) {
                if (!targetSession.getId().equals(session.getId())) {
                    sendMessage(targetSession, jsonResponse);
                }
            }
        }

        Long userId = removeSessionFromRoom(session);
        if (userId != null) {
            eventPublisher.publishEvent(new ChatSessionEndedEvent(this, userId));
        }
    }

    private void enterRoomIfNeeded(WebSocketSession session, Long roomId, User user) {
        if (roomId == null) {
            return;
        }
        if (!chatRooms.containsKey(roomId)) {
            chatRooms.put(roomId, new HashSet<>());
        }
        Set<WebSocketSession> roomSessions = chatRooms.get(roomId);
        if (!roomSessions.contains(session)) {
            roomSessions.add(session);
            sessionToRoomId.put(session, roomId);
            sessionToUserId.put(session, user.getId());
            log.info("[ChatRoom] 세션 {}가 {}번 방에 입장했습니다.", session.getId(), roomId);
        }
    }

    private void broadcastMessage(Long roomId, String messagePayload) throws Exception {
        Set<WebSocketSession> roomSessions = chatRooms.get(roomId);
        if (roomSessions != null) {
            TextMessage textMessage = new TextMessage(messagePayload);
            for (WebSocketSession s : roomSessions) {
                sendMessage(s, textMessage);
            }
        }
    }

    private void sendMessage(WebSocketSession session, String messagePayload) throws Exception {
        sendMessage(session, new TextMessage(messagePayload));
    }

    private void sendMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        try {
            if (session.isOpen()) {
                session.sendMessage(textMessage);
            }
        } catch (Exception e) {
            log.error("메시지 전송 실패. 세션 ID: {}", session.getId(), e);
            handleConnectionClosedByError(session);
        }
    }

    private User getCurrentUser(WebSocketSession session) {
        Principal principal = session.getPrincipal();
        if (principal == null || principal.getName() == null) {
            log.warn("세션에서 사용자 정보를 찾을 수 없습니다: {}", session.getId());
            try {
                if(session.isOpen()) session.close(CloseStatus.POLICY_VIOLATION);
            } catch (Exception ignored) {}
            return null;
        }
        try {
            return userRepository.findById(Long.parseLong(principal.getName()))
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
        } catch (NumberFormatException e) {
            log.error("Principal name이 Long 타입이 아닙니다: {}", principal.getName());
            try {
                if(session.isOpen()) session.close(CloseStatus.POLICY_VIOLATION);
            } catch (Exception ignored) {}
            return null;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("[WebSocket] 연결 종료. 세션 ID: {}, 상태: {}", session.getId(), status);

        Long userId = removeSessionFromRoom(session);
        if (userId != null) {
            eventPublisher.publishEvent(new ChatSessionEndedEvent(this, userId));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("[WebSocket] 전송 오류 발생. 세션 ID: {}", session.getId(), exception);

        Long userId = removeSessionFromRoom(session);
        if (userId != null) {
            eventPublisher.publishEvent(new ChatSessionEndedEvent(this, userId));
        }
    }

    private void handleConnectionClosedByError(WebSocketSession session) {
        log.warn("[WebSocket] 오류로 인해 연결 종료 처리. 세션 ID: {}", session.getId());

        Long userId = removeSessionFromRoom(session);
        if (userId != null) {
            eventPublisher.publishEvent(new ChatSessionEndedEvent(this, userId));
        }

        try {
            if (session.isOpen()) {
                session.close(CloseStatus.SERVER_ERROR);
            }
        } catch (Exception ignored) {}
    }

    private Long removeSessionFromRoom(WebSocketSession session) {
        Long userId = sessionToUserId.remove(session);
        Long roomId = sessionToRoomId.remove(session);

        if (roomId != null) {
            Set<WebSocketSession> roomSessions = chatRooms.get(roomId);
            if (roomSessions != null) {
                boolean removed = roomSessions.remove(session);
                if (removed && userId != null) {
                    log.info("[ChatRoom] 사용자 {} (세션 {})가 {}번 방에서 퇴장했습니다.", userId, session.getId(), roomId);
                }
                if (roomSessions.isEmpty()) {
                    chatRooms.remove(roomId);
                    log.info("[ChatRoom] {}번 방이 비어서 제거되었습니다.", roomId);
                }
            }
        }
        return userId;
    }
}