package com.Globoo.study.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentRes {

    private final Long id;
    private final Long postId;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final AuthorDto author;

    @Builder
    public CommentRes(Long id, Long postId, String content, LocalDateTime createdAt, LocalDateTime updatedAt, AuthorDto author) {
        this.id = id;
        this.postId = postId;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.author = author;
    }

    @Getter
    public static class AuthorDto {
        private final Long id;
        private final String nickname;
        private final String profileImageUrl;
        private final String country; // 국적추가

        @Builder
        public AuthorDto(Long id, String nickname, String profileImageUrl, String country) {
            this.id = id;
            this.nickname = nickname;
            this.profileImageUrl = profileImageUrl;
            this.country = country;
        }
    }
}
