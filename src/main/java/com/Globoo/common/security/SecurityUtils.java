// src/main/java/com/Globoo/common/security/SecurityUtils.java
package com.Globoo.common.security;

import org.springframework.security.access.AccessDeniedException;
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

    /** 로그인 유저의 ID (없으면 401) */
    public static Long requiredUserId() {
        Long id = currentUserId();
        if (id == null) throw new AccessDeniedException("Unauthenticated");
        return id;
    }
}
