package com.Globoo.study.DTO;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
public class MyCommentRes {

    // 댓글 정보
    private Long commentId;
    private String content;
    private LocalDateTime createdAt;

    // 댓글 작성자(나)
    private AuthorDto author;

    // 댓글이 달린 스터디 게시글 요약
    private StudySummaryDto study;

    @Getter
    @Builder
    public static class AuthorDto {
        private Long id;
        private String nickname;
        private String profileImageUrl;
        private String country;
    }

    @Getter
    @Builder
    public static class StudySummaryDto {
        private Long postId;
        private String title;
        private String status;
        private Integer currentParticipants;
        private Integer capacity;
        private Set<String> campuses;
        private Set<String> languages;

        // 게시글 작성자
        private PostAuthorDto author;
    }

    @Getter
    @Builder
    public static class PostAuthorDto {
        private Long id;
        private String profileImageUrl;
        private String country;
    }
}
