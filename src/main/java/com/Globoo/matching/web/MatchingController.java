package com.Globoo.matching.web;

import com.Globoo.common.web.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matching")
public class MatchingController {
    @GetMapping("/status")
    public ApiResponse<String> status(){ return ApiResponse.onSuccess("WAITING"); }
}
//ok 메소드는 존재하지 않으므로 apiResponse 메소드대로 onSuccess로 수정