package com.Globoo.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.io.Decoders; // 더 이상 필요 없음
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets; //1. import 추가
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;

    @Value("${jwt.access-token-validity-seconds:3600}")
    private long accessTokenValiditySeconds;

    //  2. 생성자(Constructor)를 아래 코드로 수정
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKeyString) {
        // Base64 디코딩 대신, 일반 텍스트(UTF-8)를 바이트로 변환합.
        byte[] keyBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(Long userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValiditySeconds * 1000);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public long getAccessTokenValiditySec() {
        return accessTokenValiditySeconds;
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }

    public Long getUserId(String token) {
        return Long.valueOf(parse(token).getPayload().getSubject());
    }

    public String getEmail(String token) {
        return parse(token).getPayload().get("email", String.class);
    }
}