// com/Globoo/auth/web/AuthController.java (일부)
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

    /* (기존) 링크 검증___이거 거슬리면삭제해야할듯여
    @GetMapping("/verify")
    public VerifyRes verify(@RequestParam String token){ return auth.verify(token); }
    */
    // 이메일 + 코드 검증
    @PostMapping("/verify-code")
    public VerifyRes verifyCode(@Valid @RequestBody VerifyCodeReq req){
        return new VerifyRes(emailVerif.verifyCode(req.email(), req.code()));
    }
}
