package com.Globoo.study.DTO;

import com.Globoo.study.domain.StudyPost;
import java.time.LocalDateTime;

public class StudyPostDto {

    // ✅ 생성/수정용 Request (언어: 단일 문자열)
    public static class Request {
        private String title;
        private String content;
        private String status;     // 모집중 / 모집완료
        private String campus;     // 서울 / 글로벌
        private String language;   // 10개 중 하나
        private Integer capacity;  // 1~6

        public Request() {}

        public Request(String title,
                       String content,
                       String status,
                       String campus,
                       String language,
                       Integer capacity) {
            this.title = title;
            this.content = content;
            this.status = status;
            this.campus = campus;
            this.language = language;
            this.capacity = capacity;
        }

        // getters
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getStatus() { return status; }
        public String getCampus() { return campus; }
        public String getLanguage() { return language; }
        public Integer getCapacity() { return capacity; }

        // setters (JSON 역직렬화용)
        public void setTitle(String title) { this.title = title; }
        public void setContent(String content) { this.content = content; }
        public void setStatus(String status) { this.status = status; }
        public void setCampus(String campus) { this.campus = campus; }
        public void setLanguage(String language) { this.language = language; }
        public void setCapacity(Integer capacity) { this.capacity = capacity; }

        // Entity 변환
        public StudyPost toEntity() {
            return new StudyPost(
                    this.title,
                    this.content,
                    this.status,
                    this.campus,
                    this.language,
                    this.capacity
            );
        }
    }

    // ✅ 응답용 Response
    public static class Response {
        private Long id;
        private String title;
        private String content;
        private String status;
        private String campus;
        private String language;
        private Integer capacity;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Response(StudyPost entity) {
            this.id = entity.getId();
            this.title = entity.getTitle();
            this.content = entity.getContent();
            this.status = entity.getStatus();
            this.campus = entity.getCampus();
            this.language = entity.getLanguage();
            this.capacity = entity.getCapacity();
            this.createdAt = entity.getCreatedAt();
            this.updatedAt = entity.getUpdatedAt();
        }

        // getters
        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getStatus() { return status; }
        public String getCampus() { return campus; }
        public String getLanguage() { return language; }
        public Integer getCapacity() { return capacity; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
    }
}
