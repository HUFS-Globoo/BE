package com.Globoo.chat.handler;

import com.Globoo.chat.dto.*;
import com.Globoo.chat.event.ChatSessionEndedEvent;
import com.Globoo.chat.service.ChatService;
import com.Globoo.common.error.EntityNotFoundException;
import com.Globoo.common.error.ErrorCode;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ChatService chatService; // 지금은 사용 안 해도 됨(나중 확장용)
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    private final Map<Long, Set<WebSocketSession>> chatRooms = new ConcurrentHashMap<>();
    private final Map<WebSocketSession, Long> sessionToRoomId = new ConcurrentHashMap<>();
    private final Map<WebSocketSession, Long> sessionToUserId = new ConcurrentHashMap<>();

    // room 종료 중복 실행 방지
    private final Set<Long> endedRooms = ConcurrentHashMap.newKeySet();

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

            if (type == null) {
                log.warn("type 없는 메시지 수신: {}", payload);
                return;
            }

            // roomId가 JOIN payload에서 chatRoomId로 올 수도 있으니 보정
            if (roomId == null && "JOIN".equals(type)) {
                if (jsonNode.has("chatRoomId")) roomId = jsonNode.get("chatRoomId").asLong();
                if (jsonNode.has("roomId")) roomId = jsonNode.get("roomId").asLong();
            }

            BaseWebSocketMessageDto baseDto = objectMapper.readValue(payload, BaseWebSocketMessageDto.class);
            User currentUser = getCurrentUser(session);
            if (currentUser == null) return;

            // DTO에 roomId 주입
            if (baseDto instanceof ChatRoomJoinReqDto dto) dto.setRoomId(roomId);
            else if (baseDto instanceof ChatMessageSendReqDto dto) dto.setRoomId(roomId);
            else if (baseDto instanceof ReadMessageReqDto dto) dto.setRoomId(roomId);
            else if (baseDto instanceof LeaveRoomReqDto dto) dto.setRoomId(roomId);

            switch (type) {
                case "JOIN" -> handleJoinMessage(session, (ChatRoomJoinReqDto) baseDto, currentUser);
                case "MESSAGE" -> handleChatMessage(session, (ChatMessageSendReqDto) baseDto, currentUser);
                case "READ" -> handleReadMessage(session, (ReadMessageReqDto) baseDto, currentUser);
                case "LEAVE" -> handleLeaveMessage(session, (LeaveRoomReqDto) baseDto, currentUser);
                default -> log.warn("알 수 없는 메시지 타입 수신: {}", type);
            }

        } catch (Exception e) {
            log.error("[handleTextMessage] 메시지 처리 중 오류 발생. 세션 ID: {}", session.getId(), e);
        }
    }

    private Long getRoomIdFromJson(JsonNode jsonNode) {
        if (jsonNode.has("roomId")) return jsonNode.get("roomId").asLong();
        if (jsonNode.has("chatRoomId")) return jsonNode.get("chatRoomId").asLong();
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
        log.info("[READ] 사용자 {}가 {}번 방 메시지를 {}번까지 읽음", reader.getId(), roomId, lastReadMessageId);

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

    /**
     * ✅ 일회성 정책: 한 명이 나가면 즉시 대화 종료
     * - 상대에게 LEAVE_NOTICE 브로드캐스트
     * - room 종료 이벤트 발행(roomId 기준) → 리스너에서 DB 정리
     */
    private void handleLeaveMessage(WebSocketSession session, LeaveRoomReqDto leaveDto, User leaver) throws Exception {
        Long roomId = leaveDto.getRoomId();
        if (roomId == null) return;

        log.info("[LEAVE] 사용자 {}가 {}번 방에서 나감", leaver.getId(), roomId);

        // 1) 상대에게 종료 알림 (기존 프로토콜 유지: LEAVE_NOTICE)
        LeaveRoomResDto leaveNoticeDto = new LeaveRoomResDto(leaver.getId(), roomId);
        String jsonResponse = objectMapper.writeValueAsString(leaveNoticeDto);

        Set<WebSocketSession> roomSessions = chatRooms.get(roomId);
        if (roomSessions != null) {
            for (WebSocketSession targetSession : roomSessions) {
                if (!targetSession.getId().equals(session.getId())) {
                    sendMessage(targetSession, jsonResponse);
                    // 선택: 즉시 상대 세션도 닫아버리면 유령 세션 확 줄어듦
                    try { targetSession.close(CloseStatus.NORMAL); } catch (Exception ignored) {}
                }
            }
        }

        // 2) 내 세션 제거
        removeSessionFromRoom(session);

        // 3) 방 종료(중복 방지) + DB 정리 이벤트 발행
        endRoomOnce(roomId);
    }

    private void enterRoomIfNeeded(WebSocketSession session, Long roomId, User user) {
        if (roomId == null) return;

        chatRooms.putIfAbsent(roomId, ConcurrentHashMap.newKeySet());
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
            if (session.isOpen()) session.sendMessage(textMessage);
        } catch (Exception e) {
            log.error("메시지 전송 실패. 세션 ID: {}", session.getId(), e);
            handleConnectionClosedByError(session);
        }
    }

    private User getCurrentUser(WebSocketSession session) {
        Principal principal = session.getPrincipal();
        if (principal == null || principal.getName() == null) {
            log.warn("세션에서 사용자 정보를 찾을 수 없습니다: {}", session.getId());
            try { if (session.isOpen()) session.close(CloseStatus.POLICY_VIOLATION); } catch (Exception ignored) {}
            return null;
        }
        try {
            return userRepository.findById(Long.parseLong(principal.getName()))
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
        } catch (NumberFormatException e) {
            log.error("Principal name이 Long 타입이 아닙니다: {}", principal.getName());
            try { if (session.isOpen()) session.close(CloseStatus.POLICY_VIOLATION); } catch (Exception ignored) {}
            return null;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long roomId = sessionToRoomId.get(session);
        log.info("[WebSocket] 연결 종료. 세션 ID: {}, 상태: {}, roomId: {}", session.getId(), status, roomId);

        removeSessionFromRoom(session);

        // 일회성: 연결 끊김도 종료로 처리
        endRoomOnce(roomId);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long roomId = sessionToRoomId.get(session);
        log.error("[WebSocket] 전송 오류 발생. 세션 ID: {}, roomId: {}", session.getId(), roomId, exception);

        removeSessionFromRoom(session);
        endRoomOnce(roomId);
    }

    private void handleConnectionClosedByError(WebSocketSession session) {
        Long roomId = sessionToRoomId.get(session);
        log.warn("[WebSocket] 오류로 인해 연결 종료 처리. 세션 ID: {}, roomId: {}", session.getId(), roomId);

        removeSessionFromRoom(session);
        endRoomOnce(roomId);

        try { if (session.isOpen()) session.close(CloseStatus.SERVER_ERROR); } catch (Exception ignored) {}
    }

    /**
     * 방 종료는 한 번만 실행되도록 가드.
     * 종료 이벤트는 roomId 기준으로 발행 → 리스너에서 DB 정리 + 매칭 NONE 처리
     */
    private void endRoomOnce(Long roomId) {
        if (roomId == null) return;
        if (!endedRooms.add(roomId)) return; // 이미 종료 처리됨

        // 메모리 맵 정리(혹시 남아있는 세션이 있으면 닫기)
        Set<WebSocketSession> sessions = chatRooms.remove(roomId);
        if (sessions != null) {
            for (WebSocketSession s : sessions) {
                sessionToRoomId.remove(s);
                sessionToUserId.remove(s);
                try { if (s.isOpen()) s.close(CloseStatus.NORMAL); } catch (Exception ignored) {}
            }
        }

        log.info("[ChatRoom] {}번 방 종료 처리(일회성). DB 정리 이벤트 발행", roomId);
        eventPublisher.publishEvent(new ChatSessionEndedEvent(this, roomId));
    }

    private void removeSessionFromRoom(WebSocketSession session) {
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
    }
}
