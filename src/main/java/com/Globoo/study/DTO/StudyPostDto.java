package com.Globoo.study.DTO;

import com.Globoo.study.domain.StudyPost;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class StudyPostDto {

    // 생성/수정용 Request (언어/캠퍼스: 리스트)
    public static class Request {
        private String title;
        private String content;
        private String status;
        private List<String> campuses; // 변경: String -> List<String>
        private List<String> languages; // 변경: String -> List<String>
        private Integer capacity;

        public Request() {}

        public Request(String title,
                       String content,
                       String status,
                       List<String> campuses,
                       List<String> languages,
                       Integer capacity) {
            this.title = title;
            this.content = content;
            this.status = status;
            this.campuses = campuses;
            this.languages = languages;
            this.capacity = capacity;
        }

        // getters
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getStatus() { return status; }
        public List<String> getCampuses() { return campuses; }   // 변경
        public List<String> getLanguages() { return languages; } // 변경
        public Integer getCapacity() { return capacity; }

        // setters (JSON 역직렬화용)
        public void setTitle(String title) { this.title = title; }
        public void setContent(String content) { this.content = content; }
        public void setStatus(String status) { this.status = status; }
        public void setCampuses(List<String> campuses) { this.campuses = campuses; }   // 변경
        public void setLanguages(List<String> languages) { this.languages = languages; } // 변경
        public void setCapacity(Integer capacity) { this.capacity = capacity; }
    }

    // 응답용 Response
    public static class Response {
        private Long id;
        private String title;
        private String content;
        private String status;
        private Set<String> campuses; // 변경: String -> Set<String>
        private Set<String> languages; // 변경: String -> Set<String>
        private Integer capacity;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // 작성자 정보 필드 추가
        private Long authorId;
        private String authorNickname;
        private String authorProfileImageUrl;

        public Response(StudyPost entity) {
            this.id = entity.getId();
            this.title = entity.getTitle();
            this.content = entity.getContent();
            this.status = entity.getStatus();
            this.campuses = entity.getCampuses();
            this.languages = entity.getLanguages();
            this.capacity = entity.getCapacity();
            this.createdAt = entity.getCreatedAt();
            this.updatedAt = entity.getUpdatedAt();

            // User 및 Profile 정보 매핑 (Null-safe)
            if (entity.getUser() != null) {
                this.authorId = entity.getUser().getId();

                // Profile이 존재할 경우 닉네임과 이미지 URL 설정
                if (entity.getUser().getProfile() != null) {
                    this.authorNickname = entity.getUser().getProfile().getNickname();
                    this.authorProfileImageUrl = entity.getUser().getProfile().getProfileImage();
                } else {
                    // Profile이 없는 경우 User의 name 또는 username을 fallback으로 사용
                    this.authorNickname = entity.getUser().getName(); // 또는 .getUsername()
                }
            }
        }

        // getters
        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getStatus() { return status; }
        public Set<String> getCampuses() { return campuses; }
        public Set<String> getLanguages() { return languages; }
        public Integer getCapacity() { return capacity; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }

        // 추가된 필드 Getter
        public Long getAuthorId() { return authorId; }
        public String getAuthorNickname() { return authorNickname; }
        public String getAuthorProfileImageUrl() { return authorProfileImageUrl; }
    }
}