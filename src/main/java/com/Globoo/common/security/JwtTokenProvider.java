package com.Globoo.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.token-validity-in-ms}")
    private long tokenValidityInMilliseconds;

    private final UserDetailsService userDetailsService;

    private Key key; // ⬅️ Key 객체로 변경

    public JwtTokenProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @PostConstruct
    protected void init() {
        // ⬇️ secretKey를 Base64 디코딩하여 Key 객체로 변환
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        // ⬆️
    }

    // JWT 토큰 생성
    public String createToken(String userId) {
        // ⬇️ 오류 수정: .build() 추가
        Claims claims = Jwts.claims().setSubject(userId).build();
        // ⬆️
        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                // ⬇️ 수정됨: Key 객체와 알고리즘 명시
                .signWith(key, SignatureAlgorithm.HS256)
                // ⬆️
                .compact();
    }

    // 토큰에서 인증 정보 조회
    public Authentication getAuthentication(String token) {
        String userId = getUserIdFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
        return new UsernamePasswordAuthenticationToken(userDetails.getUsername(), "", userDetails.getAuthorities());
    }

    // 토큰에서 회원 ID 추출
    public String getUserIdFromToken(String token) {
        // ⬇️ 수정됨: Jwts.parserBuilder() 사용
        return Jwts.parser()
                .setSigningKey(key)
                .build() // parser 생성
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        // ⬆️
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            // ⬇️ 수정됨: Jwts.parserBuilder() 사용
            Jwts.parser()
                    .setSigningKey(key)
                    .build() // parser 생성
                    .parseClaimsJws(token);
            // ⬆️
            return true;
        } catch (Exception e) {
            // (로그 처리)
            return false;
        }
    }

    // Request 헤더에서 토큰 값 가져오기
    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}