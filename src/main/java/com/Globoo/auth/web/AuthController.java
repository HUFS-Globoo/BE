package com.Globoo.auth.web;


import com.Globoo.common.web.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @PostMapping("/login")
    public ApiResponse<String> login(){ return ApiResponse.ok("token"); }
}
