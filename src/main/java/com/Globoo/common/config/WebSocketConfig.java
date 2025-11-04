package com.Globoo.common.config;

import com.Globoo.chat.handler.ChatHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.config.annotation.*;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker   // STOMP용 (매칭 등)
@EnableWebSocket               // 순수 WebSocket용 (ChatHandler)
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer, WebSocketConfigurer {

    private final ChatHandler chatHandler;

    /**
     * 순수 WebSocket 엔드포인트 (ChatHandler)
     *  - 여기로 오는 건 STOMP가 아니라 ChatHandler가 직접 처리
     *  - Postman / 프론트에서 JSON 보내는 주소: ws://localhost:8080/ws/chat
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler, "/ws/chat")
                .setAllowedOrigins("*");
    }

    /**
     * STOMP 엔드포인트 (매칭용 등)
     *  - STOMP 클라이언트는 ws://localhost:8080/ws/match 사용
     *  - IMPORTANT: /ws/chat 은 STOMP에서 제거 (이제 ChatHandler 전용)
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/match").setAllowedOriginPatterns("*");
    }

    /**
     * STOMP 메시지 브로커 설정 (매칭용)
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub", "/queue");
        registry.setApplicationDestinationPrefixes("/pub");
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * STOMP CONNECT 시 userId 헤더를 Principal로 설정
     *  - 매칭 쪽 STOMP 클라이언트에서만 사용
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    List<String> userIds = accessor.getNativeHeader("userId");
                    if (userIds != null && !userIds.isEmpty()) {
                        String uid = userIds.get(0);
                        accessor.setUser(new UsernamePasswordAuthenticationToken(uid, null));
                    }
                }
                return message;
            }
        });
    }
}


//정리 랜덤매칭-stomp 사용 / 채팅-websocket 사용