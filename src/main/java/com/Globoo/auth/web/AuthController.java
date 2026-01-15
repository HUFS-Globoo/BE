package com.Globoo.auth.web;

import com.Globoo.auth.dto.*;
import com.Globoo.auth.service.AuthService;
import com.Globoo.auth.service.EmailVerificationService;
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

    @PostMapping("/signup")
    public SignupRes signup(@Valid @RequestBody SignupReq req){ return auth.signup(req); }

    @PostMapping("/login")
    public TokenRes login(@Valid @RequestBody LoginReq req){ return auth.login(req); }

    @PostMapping("/refresh")
    public TokenRes refresh(@Valid @RequestBody RefreshReq req){ return auth.refresh(req.refreshToken()); }

    @PostMapping("/logout")
    public OkRes logout(@Valid @RequestBody RefreshReq req){ return auth.logout(req.refreshToken()); }

    @PostMapping("/verification/resend")
    public OkRes resend(@Valid @RequestBody ResendReq req){ return auth.resend(req); }

    // 이메일 + 코드 검증 ( 성공 시 User 생성)
    @PostMapping("/verify-code")
    public VerifyRes verifyCode(@Valid @RequestBody VerifyCodeReq req){
        Long userId = emailVerif.verifyCodeAndCreateUser(req.email(), req.code());
        return new VerifyRes(true, userId);
    }
}
