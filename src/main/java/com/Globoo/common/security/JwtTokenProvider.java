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

    public JwtTokenProvider(@Value("${jwt.secret}") String base64Secret) {
        byte[] keyBytes = Decoders.BASE64.decode(base64Secret); // Base64 decode!
        this.key = Keys.hmacShaKeyFor(keyBytes);                //32B 이상이면 OK
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
