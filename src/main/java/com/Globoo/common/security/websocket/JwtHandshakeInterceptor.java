package com.Globoo.common.security.websocket;

import com.Globoo.common.security.JwtTokenProvider;
import com.Globoo.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            return false;
        }

        try {
            jwtTokenProvider.parse(token);
        } catch (Exception e) {
            return false;
        }

        // 채팅 WebSocket은 로그인 Access 토큰만 허용
        if (!jwtTokenProvider.isAccessToken(token)) {
            return false;
        }

        Long userId;
        try {
            userId = jwtTokenProvider.getUserId(token);
        } catch (Exception e) {
            return false;
        }

        if (userId == null || !userRepository.existsById(userId)) {
            return false;
        }

        attributes.put("userId", String.valueOf(userId));
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // no-op
    }

    private String resolveToken(ServerHttpRequest request) {
        // 1) Authorization: Bearer ...
        String bearer = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        // 2) ?token=...
        if (request instanceof ServletServerHttpRequest servletReq) {
            HttpServletRequest httpServletRequest = servletReq.getServletRequest();

            String queryToken = httpServletRequest.getParameter("token");
            if (StringUtils.hasText(queryToken)) {
                return queryToken;
            }

            // 3) Cookie: accessToken / token
            Cookie[] cookies = httpServletRequest.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if (c == null) continue;
                    String name = c.getName();
                    if (!StringUtils.hasText(name)) continue;

                    if ("accessToken".equals(name) || "token".equals(name)) {
                        String value = c.getValue();
                        if (StringUtils.hasText(value)) {
                            return value;
                        }
                    }
                }
            }
        }

        return null;
    }
}

