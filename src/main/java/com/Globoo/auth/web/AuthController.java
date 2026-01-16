package com.Globoo.auth.web;

import com.Globoo.auth.dto.*;
import com.Globoo.auth.service.AuthService;
import com.Globoo.auth.service.EmailVerificationService;
import com.Globoo.common.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name="Auth")
public class AuthController {
    private final AuthService auth;
    private final EmailVerificationService emailVerif;
    private final JwtTokenProvider jwt;

    // Step1: 기본정보만 받음 + 이메일 코드 발송
    @PostMapping("/signup")
    public SignupRes signup(@Valid @RequestBody SignupStep1Req req){
        return auth.signup(req);
    }

    @PostMapping("/login")
    public TokenRes login(@Valid @RequestBody LoginReq req){ return auth.login(req); }

    @PostMapping("/refresh")
    public TokenRes refresh(@Valid @RequestBody RefreshReq req){ return auth.refresh(req.refreshToken()); }

    @PostMapping("/logout")
    public OkRes logout(@Valid @RequestBody RefreshReq req){ return auth.logout(req.refreshToken()); }

    @PostMapping("/verification/resend")
    public OkRes resend(@Valid @RequestBody ResendReq req){ return auth.resend(req); }

    // Step2: 이메일 + 코드 + 캠퍼스 검증 (성공 시 User/Profile 생성) + 온보딩 토큰 발급
    @PostMapping("/verify-code")
    public VerifyRes verifyCode(@Valid @RequestBody VerifyCodeReq req){
        Long userId = emailVerif.verifyCodeAndCreateUser(req.email(), req.code(), req.campus());
        String onboardingToken = jwt.createOnboardingToken(userId, req.email());
        return new VerifyRes(true, userId, onboardingToken);
    }
}
