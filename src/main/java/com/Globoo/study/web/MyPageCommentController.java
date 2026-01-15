package com.Globoo.study.web;

import com.Globoo.common.error.BaseException;
import com.Globoo.common.error.ErrorCode;
import com.Globoo.common.web.ApiResponse;
import com.Globoo.study.DTO.MyCommentRes;
import com.Globoo.study.service.CommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "MyPage")
public class MyPageCommentController {

    private final CommentService commentService;

    @GetMapping("/api/users/me/comments")
    public ApiResponse<List<MyCommentRes>> myComments(
            @AuthenticationPrincipal Long currentUserId
    ) {
        if (currentUserId == null) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        return ApiResponse.onSuccess(
                commentService.getMyCommentsForMyPage(currentUserId)
        );
    }
}
