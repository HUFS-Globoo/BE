package com.Globoo.user.web;


import com.Globoo.common.web.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @GetMapping("/me")
    public ApiResponse<String> me(){ return ApiResponse.onSuccess("me"); }
}
