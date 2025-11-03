package com.Globoo.study.web;


import com.Globoo.study.DTO.CommentPageRes;
import com.Globoo.study.DTO.CommentRes;
import com.Globoo.study.DTO.CommentReq;
import com.Globoo.study.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/study/posts/{postId}/comments")
@RequiredArgsConstructor
@Tag(name="Comment")
public class CommentController {

    private final CommentService commentService;

    /** 댓글 생성 */
    @PostMapping
    @Operation(summary = "댓글 생성", description = "특정 스터디 게시글(postId)에 새로운 댓글을 작성합니다.")
    public ResponseEntity<CommentRes> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentReq request,
            @AuthenticationPrincipal Long authorId) {

        CommentRes response = commentService.createComment(postId, request, authorId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** 댓글 단건 조회 */
    @GetMapping("/{commentId}")
    @Operation(summary = "댓글 단건 조회", description = "특정 스터디 게시글(postId)에 속한 단일 댓글(commentId)의 상세 정보를 조회 합니다.")
    public ResponseEntity<CommentRes> getCommentById(
            @PathVariable Long postId,
            @PathVariable Long commentId) {

        CommentRes response = commentService.getComment(postId, commentId);

        return ResponseEntity.ok(response);
    }

    /** 댓글 목록 페이징 조회 */
    @GetMapping
    @Operation(summary = "댓글 조회", description = "특정 스터디 게시글(postId)에 속한 댓글 들의 정보를 조회 합니다.")
    public ResponseEntity<CommentPageRes> getCommentsByPostId(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "asc") String order) {

        CommentPageRes response = commentService.getComments(postId, page, size, sort, order);

        return ResponseEntity.ok(response);
    }

    /** 댓글 수정 */
    @PatchMapping("/{commentId}")
    @Operation(summary = "댓글 수정", description = "특정 스터디 게시글(postId)에 속한 단일 댓글(commentId) 정보를 수정 합니다. 댓글 작성 본인 또는 관리자만 수정할 수 있습니다.")
    public ResponseEntity<CommentRes> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentReq request,
            @AuthenticationPrincipal Long currentUserId) { // 인증된 사용자 ID

        // Service에서 currentUserId를 받아 권한 검증
        CommentRes response = commentService.updateComment(postId, commentId, request, currentUserId);

        return ResponseEntity.ok(response);
    }

    /** 댓글 삭제 */
    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제", description = "특정 스터디 게시글(postId)에 속한 단일 댓글(commentId) 정보를 삭제 합니다. 댓글 작성 본인 또는 관리자만 삭제 할 수 있습니다.")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal Long currentUserId) { // 인증된 사용자 ID

        // Service에서 권한 검증 및 삭제 처리
        commentService.deleteComment(postId, commentId, currentUserId);

        // 성공 시 204 No Content 응답
        return ResponseEntity.noContent().build();
    }

}