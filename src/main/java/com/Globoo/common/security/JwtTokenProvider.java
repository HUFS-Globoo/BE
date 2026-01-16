package com.Globoo.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;

    @Value("${jwt.access-token-validity-seconds:3600}")
    private long accessTokenValiditySeconds;

    @Value("${jwt.onboarding-token-validity-seconds:1800}") // 30분
    private long onboardingTokenValiditySeconds;

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_TYPE  = "type";
    private static final String TYPE_ACCESS = "ACCESS";
    private static final String TYPE_ONBOARDING = "ONBOARDING";

    public JwtTokenProvider(@Value("${jwt.secret}") String base64Secret) {
        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /** 로그인용 Access Token */
    public String createAccessToken(Long userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValiditySeconds * 1000);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /** 온보딩(Step3/4) 전용 토큰: 로그인 아님 */
    public String createOnboardingToken(Long userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + onboardingTokenValiditySeconds * 1000);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_TYPE, TYPE_ONBOARDING)
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
        return parse(token).getPayload().get(CLAIM_EMAIL, String.class);
    }

    public String getType(String token) {
        return parse(token).getPayload().get(CLAIM_TYPE, String.class);
    }

    public boolean isAccessToken(String token) {
        return TYPE_ACCESS.equals(getType(token));
    }

    public boolean isOnboardingToken(String token) {
        return TYPE_ONBOARDING.equals(getType(token));
    }
}
