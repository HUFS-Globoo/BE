package com.Globoo.study.web;

import com.Globoo.common.web.ApiResponse;
import com.Globoo.study.DTO.MyStudyCardRes;
import com.Globoo.study.service.MyStudyService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me/studies")
public class MyStudyController {

    private final MyStudyService myStudyService;

    public MyStudyController(MyStudyService myStudyService) {
        this.myStudyService = myStudyService;
    }

    @Operation(summary = "마이페이지 - 내가 신청한 스터디 목록")
    @GetMapping("/applied")
    public ApiResponse<List<MyStudyCardRes>> applied(@AuthenticationPrincipal Long currentUserId) {
        return ApiResponse.onSuccess(myStudyService.getApplied(currentUserId));
    }

    @Operation(summary = "마이페이지 - 내가 올린 스터디 목록")
    @GetMapping("/owned")
    public ApiResponse<List<MyStudyCardRes>> owned(@AuthenticationPrincipal Long currentUserId) {
        return ApiResponse.onSuccess(myStudyService.getOwned(currentUserId));
    }
}
