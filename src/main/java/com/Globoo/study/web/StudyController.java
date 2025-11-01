package com.Globoo.study.web;

import com.Globoo.common.web.ApiResponse;
import com.Globoo.study.DTO.StudyPostDto;
import com.Globoo.study.service.StudyService;
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
     * 목록 조회
     * - status=open/모집중/closed/모집완료
     * - campus=global/글로벌/seoul/서울
     * - language=허용 10개 중 하나
     * - capacity 범위 (minCapacity ≤ capacity ≤ maxCapacity)
     */
    @GetMapping
    public ApiResponse<List<StudyPostDto.Response>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String campus,
            @RequestParam(required = false) String language,   // 단일 언어
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) Integer maxCapacity
    ) {
        return ApiResponse.ok(
                studyService.getStudyPosts(status, campus, language, minCapacity, maxCapacity)
        );
    }

    @GetMapping("/{postId}")
    public ApiResponse<StudyPostDto.Response> getOne(@PathVariable Long postId) {
        return ApiResponse.ok(studyService.getStudyPost(postId));
    }

    @PostMapping
    public ApiResponse<StudyPostDto.Response> create(@RequestBody StudyPostDto.Request req) {
        return ApiResponse.ok(studyService.createStudyPost(req));
    }

    @PatchMapping("/{postId}")
    public ApiResponse<StudyPostDto.Response> update(
            @PathVariable Long postId,
            @RequestBody StudyPostDto.Request req
    ) {
        return ApiResponse.ok(studyService.updateStudyPost(postId, req));
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<String> delete(@PathVariable Long postId) {
        studyService.deleteStudyPost(postId);
        return ApiResponse.ok("게시글이 성공적으로 삭제되었습니다.");
    }
}
