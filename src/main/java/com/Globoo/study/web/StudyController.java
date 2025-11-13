package com.Globoo.study.web;

import com.Globoo.common.web.ApiResponse;
import com.Globoo.study.DTO.StudyPostDto;
import com.Globoo.study.service.StudyService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/studies")
public class StudyController {

    private final StudyService studyService;

    public StudyController(StudyService studyService) {
        this.studyService = studyService;
    }

    /**
     * 목록 조회 (UI에 맞게 인원수 필터 제거)
     */
    @Operation(summary = "스터디 목록 조회 (필터링)")
    @GetMapping
    public ApiResponse<List<StudyPostDto.Response>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) List<String> campus,
            @RequestParam(required = false) List<String> language

    ) {
        //  (수정) 불필요한 null, null 제거
        return ApiResponse.onSuccess(
                studyService.getStudyPosts(status, campus, language)
        );
    }

    /**
     * 단일 조회
     */
    @Operation(summary = "스터디 단일 조회")
    @GetMapping("/{postId}")
    public ApiResponse<StudyPostDto.Response> getOne(@PathVariable Long postId) {
        return ApiResponse.onSuccess(studyService.getStudyPost(postId));
    }

    /**
     * 생성
     */
    @Operation(summary = "스터디 생성")
    @PostMapping
    public ApiResponse<StudyPostDto.Response> create(
            @RequestBody StudyPostDto.Request req,
            @AuthenticationPrincipal Long currentUserId
    ) {
        return ApiResponse.onSuccess(studyService.createStudyPost(req, currentUserId));
    }

    /**
     * 수정
     */
    @Operation(summary = "스터디 수정 (본인)")
    @PatchMapping("/{postId}")
    public ApiResponse<StudyPostDto.Response> update(
            @PathVariable Long postId,
            @RequestBody StudyPostDto.Request req,
            @AuthenticationPrincipal Long currentUserId
    ) {
        return ApiResponse.onSuccess(studyService.updateStudyPost(postId, req, currentUserId));
    }

    /**
     * 삭제
     */
    @Operation(summary = "스터디 삭제 (본인)")
    @DeleteMapping("/{postId}")
    public ApiResponse<String> delete(
            @PathVariable Long postId,
            @AuthenticationPrincipal Long currentUserId
    ) {
        studyService.deleteStudyPost(postId, currentUserId);
        return ApiResponse.onSuccess("게시글이 성공적으로 삭제되었습니다.");
    }
}