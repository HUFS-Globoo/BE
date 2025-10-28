package com.Globoo.common.security;

import com.Globoo.common.error.ErrorCode;
import com.Globoo.common.error.exception.AuthorizationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    // SecurityContext에서 현재 인증된 사용자의 ID를 가져옴
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            // 인증 정보가 없는 경우 (토큰이 없거나 유효하지 않음)
            throw new AuthorizationException(ErrorCode.UNAUTHORIZED);
        }

        try {
            // Principal은 String 타입("1")이므로 Long으로 파싱
            return Long.parseLong((String) authentication.getPrincipal());
        } catch (NumberFormatException | ClassCastException e) {
            // Principal이 예상과 다른 타입일 경우
            throw new AuthorizationException(ErrorCode.UNAUTHORIZED);
        }
    }
}