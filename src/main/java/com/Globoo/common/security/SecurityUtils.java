// src/main/java/com/Globoo/common/security/SecurityUtils.java
package com.Globoo.common.security;

import com.Globoo.common.error.AuthException;
import com.Globoo.common.error.ErrorCode;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public final class SecurityUtils {

    private SecurityUtils() {}

    /** 로그인 유저의 ID (없으면 null) */
    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext() != null
                ? SecurityContextHolder.getContext().getAuthentication()
                : null;
        if (auth == null || auth.getPrincipal() == null) return null;

        Object principal = auth.getPrincipal();

        // JwtAuthenticationFilter가 principal을 Long userId로 세팅
        if (principal instanceof Long id) return id;

        // 혹시 기존 코드에서 UserDetails/문자열로 넣는 경우까지 호환
        if (principal instanceof UserDetails ud) {
            try { return Long.parseLong(ud.getUsername()); } catch (NumberFormatException ignored) {}
        }
        if (principal instanceof String s) {
            try { return Long.parseLong(s); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    //에러 일관성 지키기위해 401
    /** 로그인 유저의 ID (없으면 401) */
    public static Long requiredUserId() {
        Long id = currentUserId();
        if (id == null) throw new AuthException(ErrorCode.UNAUTHORIZED); // 변경됨
        return id;
    }

    // SecurityContext에서 현재 인증된 사용자의 ID를 가져옴
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) { // 변경됨
            // 인증 정보가 없는 경우 (토큰이 없거나 유효하지 않음)
            throw new AuthException(ErrorCode.UNAUTHORIZED);
        }

        try {
            // Principal은 String 타입("1")이므로 Long으로 파싱
            return Long.parseLong((String) authentication.getPrincipal());
        } catch (NumberFormatException | ClassCastException e) {
            // Principal이 예상과 다른 타입일 경우
            throw new AuthException(ErrorCode.UNAUTHORIZED);
        }
    }
}
