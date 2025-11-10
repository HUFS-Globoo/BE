// src/main/java/com/Globoo/common/security/JwtAuthenticationFilter.java
package com.Globoo.common.security;

import com.Globoo.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;   // 추가

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   UserRepository userRepository) {   // 생성자 수정
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (StringUtils.hasText(token) && isValid(token)) {
            // JwtTokenProvider 시그니처에 맞춰 userId 추출
            Long userId = jwtTokenProvider.getUserId(token);

            if (userId != null) {

                // userId가 실제 DB에 존재하는지 확인
                boolean exists = userRepository.existsById(userId);
                if (!exists) {
                    // 존재하지 않는 유저를 가리키는 토큰이면 인증 세팅하지 않고 패스
                    // (결국 컨트롤러 단에서 401/403 발생)
                    filterChain.doFilter(request, response);
                    return;
                }

                // 필요 시 ROLE_USER 외 권한 확장 가능
                List<SimpleGrantedAuthority> authorities =
                        List.of(new SimpleGrantedAuthority("ROLE_USER"));

                // principal 을 Long userId 로 세팅 → SecurityUtils.currentUserId()에서 그대로 사용
                Authentication auth =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isValid(String token) {
        try {
            // 파싱에 성공하면 유효한 토큰
            jwtTokenProvider.parse(token);
            return true;
        } catch (Exception e) {
            // 로그 찍고 싶으면 여기서 찍어도 됨
            return false;
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
