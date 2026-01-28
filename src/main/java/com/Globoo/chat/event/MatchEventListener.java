package com.Globoo.chat.event;

import com.Globoo.matching.web.MatchingSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchEventListener {

    private final MatchingSocketHandler socketHandler;

    /**  1단계: 매칭 상대 발견 알림 (FOUND) */
    @EventListener
    public void handleMatchFound(MatchFoundEvent event) {
        Map<String, Object> payload = Map.of(
                "status", "FOUND",
                "matchId", event.getMatch().getId(),
                "profileA", event.getProfileA(),
                "profileB", event.getProfileB()
        );
        // 유저 A, B에게 각각 소켓 전송
        socketHandler.sendToUser(event.getMatch().getUserAId(), payload);
        socketHandler.sendToUser(event.getMatch().getUserBId(), payload);
        log.info("[Socket] Match Found 알림 전송 완료");
    }

    /**  2단계: 양쪽 수락 시 채팅방 이동 알림 (CHATTING) */
    @EventListener
    public void handleChatReady(ChatReadyEvent event) {
        Map<String, Object> payload = Map.of(
                "status", "CHATTING",
                "matchId", event.getMatch().getId(),
                "chatRoomId", event.getMatch().getChatRoomId()
        );
        socketHandler.sendToUser(event.getMatch().getUserAId(), payload);
        socketHandler.sendToUser(event.getMatch().getUserBId(), payload);
        log.info("[Socket] Chat Ready 알림 전송 완료");
    }
}