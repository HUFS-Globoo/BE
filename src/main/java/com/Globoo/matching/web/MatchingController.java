package com.Globoo.matching.web;


import com.Globoo.common.web.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matching")
public class MatchingController {
    @GetMapping("/status")
    public ApiResponse<String> status(){ return ApiResponse.ok("WAITING"); }
}
