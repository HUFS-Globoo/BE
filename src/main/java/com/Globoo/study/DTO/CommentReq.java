package com.Globoo.study.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentReq {

    @NotBlank(message = "댓글 내용이 비어있습니다.")
    private String content;

    public CommentReq(String content) {
        this.content = content;
    }
}
