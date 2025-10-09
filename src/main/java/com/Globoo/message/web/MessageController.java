package com.Globoo.message.web;


import com.Globoo.common.web.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    @GetMapping
    public ApiResponse<String> list(){ return ApiResponse.ok("[]"); }
}
