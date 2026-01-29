package com.Globoo.common.security.websocket;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.http.server.ServerHttpRequest;

import java.security.Principal;
import java.util.Map;

@Component
public class JwtHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(
            ServerHttpRequest request,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        Object userId = attributes.get("userId");
        if (userId == null) {
            return null;
        }
        // ChatHandler는 principal.getName()을 Long 파싱해서 사용
        return new UsernamePasswordAuthenticationToken(String.valueOf(userId), null);
    }
}

