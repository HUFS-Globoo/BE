package com.Globoo.study.web;

import com.Globoo.common.web.ApiResponse;
import com.Globoo.study.DTO.StudyPostDto;
import com.Globoo.study.service.StudyService;

// ✅ Spring Security의 @AuthenticationPrincipal 임포트
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
     * 목록 조회 (다중 필터 가능)
     */
    @GetMapping
    public ApiResponse<List<StudyPostDto.Response>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) List<String> campus,
            @RequestParam(required = false) List<String> language,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) Integer maxCapacity
    ) {
        return ApiResponse.onSuccess(
                studyService.getStudyPosts(status, campus, language, minCapacity, maxCapacity)
        );
    }

    /**
     * 단일 조회
     */
    @GetMapping("/{postId}")
    public ApiResponse<StudyPostDto.Response> getOne(@PathVariable Long postId) {
        return ApiResponse.onSuccess(studyService.getStudyPost(postId));
    }

    /**
     * 생성
     */
    @PostMapping
    public ApiResponse<StudyPostDto.Response> create(
            @RequestBody StudyPostDto.Request req,
            //  Long 타입으로 사용자 ID를 직접 받음
            @AuthenticationPrincipal Long currentUserId
    ) {
        return ApiResponse.onSuccess(studyService.createStudyPost(req, currentUserId));
    }

    /**
     * 수정
     */
    @PatchMapping("/{postId}")
    public ApiResponse<StudyPostDto.Response> update(
            @PathVariable Long postId,
            @RequestBody StudyPostDto.Request req,
            //  Long 타입으로 사용자 ID를 직접 받음
            @AuthenticationPrincipal Long currentUserId
    ) {
        return ApiResponse.onSuccess(studyService.updateStudyPost(postId, req, currentUserId));
    }

    /**
     * 삭제
     */
    @DeleteMapping("/{postId}")
    public ApiResponse<String> delete(
            @PathVariable Long postId,
            //  Long 타입으로 사용자 ID를 직접 받음
            @AuthenticationPrincipal Long currentUserId
    ) {
        studyService.deleteStudyPost(postId, currentUserId);
        return ApiResponse.onSuccess("게시글이 성공적으로 삭제되었습니다.");
    }
}