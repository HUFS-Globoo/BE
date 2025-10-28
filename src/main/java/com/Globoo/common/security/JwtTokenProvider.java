package com.Globoo.common.security;


import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    public String createAccessToken(Long userId){ return "stub"; }
    public boolean validate(String token){ return true; }
    public Long getUserId(String token){ return 0L; }
}
