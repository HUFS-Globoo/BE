package com.Globoo.auth.web;

import com.Globoo.auth.dto.AuthLoginReqDto;
import com.Globoo.auth.dto.AuthLoginResDto;
import com.Globoo.auth.service.AuthService;
import com.Globoo.common.web.ApiResponse;
import lombok.RequiredArgsConstructor; // ⬅️ 추가
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor // ⬅️ 추가
public class AuthController {

    private final AuthService authService; // ⬅️ 주입

    @PostMapping("/login")
    // ⬇️ 수정
    public ApiResponse<AuthLoginResDto> login(@RequestBody AuthLoginReqDto reqDto){
        AuthLoginResDto resDto = authService.login(reqDto);
        return ApiResponse.onSuccess(resDto);
    }
}