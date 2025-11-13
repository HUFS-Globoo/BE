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
        private List<String> campuses;
        private List<String> languages;
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
        public List<String> getCampuses() { return campuses; }
        public List<String> getLanguages() { return languages; }
        public Integer getCapacity() { return capacity; }

        // setters (JSON 역직렬화용)
        public void setTitle(String title) { this.title = title; }
        public void setContent(String content) { this.content = content; }
        public void setStatus(String status) { this.status = status; }
        public void setCampuses(List<String> campuses) { this.campuses = campuses; }
        public void setLanguages(List<String> languages) { this.languages = languages; }
        public void setCapacity(Integer capacity) { this.capacity = capacity; }
    }

    // 응답용 Response
    public static class Response {
        private Long id;
        private String title;
        private String content;
        private String status;
        private Set<String> campuses;
        private Set<String> languages;
        private Integer capacity;

        // ✅ (추가) 현재 참여 인원 필드
        private Integer currentParticipants;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
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

            // ✅ (추가) members Set의 크기를 현재 인원수로 매핑
            this.currentParticipants = entity.getMembers().size();

            this.createdAt = entity.getCreatedAt();
            this.updatedAt = entity.getUpdatedAt();

            if (entity.getUser() != null) {
                this.authorId = entity.getUser().getId();
                if (entity.getUser().getProfile() != null) {
                    this.authorNickname = entity.getUser().getProfile().getNickname();
                    this.authorProfileImageUrl = entity.getUser().getProfile().getProfileImage();
                } else {
                    this.authorNickname = entity.getUser().getName();
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
        public Long getAuthorId() { return authorId; }
        public String getAuthorNickname() { return authorNickname; }
        public String getAuthorProfileImageUrl() { return authorProfileImageUrl; }

        // ✅ (추가) currentParticipants Getter
        public Integer getCurrentParticipants() {
            return currentParticipants;
        }
    }
}