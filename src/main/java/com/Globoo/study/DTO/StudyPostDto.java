package com.Globoo.study.DTO;

import com.Globoo.study.domain.StudyPost;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class StudyPostDto {

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

        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getStatus() { return status; }
        public List<String> getCampuses() { return campuses; }
        public List<String> getLanguages() { return languages; }
        public Integer getCapacity() { return capacity; }

        public void setTitle(String title) { this.title = title; }
        public void setContent(String content) { this.content = content; }
        public void setStatus(String status) { this.status = status; }
        public void setCampuses(List<String> campuses) { this.campuses = campuses; }
        public void setLanguages(List<String> languages) { this.languages = languages; }
        public void setCapacity(Integer capacity) { this.capacity = capacity; }
    }

    public static class Response {
        private Long id;
        private String title;
        private String content;
        private String status;
        private Set<String> campuses;
        private Set<String> languages;
        private Integer capacity;

        private Integer currentParticipants;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        private Long authorId;
        private String authorNickname;
        private String authorProfileImageUrl;

        //  추가: 작성자 국적
        private String authorCountry;

        public Response(StudyPost entity) {
            this.id = entity.getId();
            this.title = entity.getTitle();
            this.content = entity.getContent();
            this.status = entity.getStatus();
            this.campuses = entity.getCampuses();
            this.languages = entity.getLanguages();
            this.capacity = entity.getCapacity();

            this.currentParticipants = entity.getMembers().size();

            this.createdAt = entity.getCreatedAt();
            this.updatedAt = entity.getUpdatedAt();

            if (entity.getUser() != null) {
                this.authorId = entity.getUser().getId();

                if (entity.getUser().getProfile() != null) {
                    this.authorNickname = entity.getUser().getProfile().getNickname();
                    this.authorProfileImageUrl = entity.getUser().getProfile().getProfileImage();

                    // ✅ 여기 한 줄이 핵심
                    this.authorCountry = entity.getUser().getProfile().getCountry();

                } else {
                    this.authorNickname = entity.getUser().getName();
                    this.authorCountry = null;
                }
            }
        }

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

        public Integer getCurrentParticipants() { return currentParticipants; }

        // ✅ 추가 getter
        public String getAuthorCountry() { return authorCountry; }
    }
}
