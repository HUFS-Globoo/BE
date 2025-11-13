package com.Globoo.matching.web;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class MatchingSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * ✅ 특정 유저에게 WebSocket 메시지 전송
     * @param userId  대상 유저 ID
     * @param payload 전송할 데이터(Map 형식)
     */
    public void sendToUser(Long userId, Map<String, Object> payload) {
        // Spring STOMP 규칙에 따라 convertAndSendToUser 사용
        // 클라이언트는 /user/queue/matching 구독 중이어야 함
        messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                "/queue/matching",
                payload
        );
    }
}
