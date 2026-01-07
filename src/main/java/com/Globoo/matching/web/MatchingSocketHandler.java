package com.Globoo.matching.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchingSocketHandler {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendToUser(Long userId, Map<String, Object> payload) {
        try {
            messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/matching", payload);
            log.info("[Socket] Matching message sent to userId: {}", userId);
        } catch (Exception e) {
            log.error("[Socket] Failed to send matching message to userId: {}, Error: {}", userId, e.getMessage());
        }
    }
}