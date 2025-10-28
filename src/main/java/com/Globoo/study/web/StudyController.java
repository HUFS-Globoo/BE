package com.Globoo.study.web;


import com.Globoo.common.web.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/studies")
public class StudyController {
    @GetMapping
    public ApiResponse<String> list(){ return ApiResponse.onSuccess("[]"); }
}
