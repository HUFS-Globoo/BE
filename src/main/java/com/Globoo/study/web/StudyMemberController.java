package com.Globoo.study.web;

import com.Globoo.common.web.ApiResponse;
import com.Globoo.study.DTO.StudyApplicantRes;
import com.Globoo.study.service.StudyMemberService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/studies/{postId}")
public class StudyMemberController {

    private final StudyMemberService studyMemberService;

    public StudyMemberController(StudyMemberService studyMemberService) {
        this.studyMemberService = studyMemberService;
    }

    @Operation(summary = "스터디 가입하기") // ✅
    @PostMapping("/join")
    public ApiResponse<String> joinStudy(
            @PathVariable Long postId,
            @AuthenticationPrincipal Long currentUserId
    ) {
        studyMemberService.joinStudy(postId, currentUserId);
        return ApiResponse.onSuccess("스터디에 성공적으로 가입되었습니다.");
    }

    @GetMapping("/members")
    public ApiResponse<List<StudyApplicantRes>> members(@PathVariable Long postId) {
        return ApiResponse.onSuccess(studyMemberService.getApplicants(postId));
    }

}