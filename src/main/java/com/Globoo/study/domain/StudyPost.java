package com.Globoo.study.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "study_posts")
public class StudyPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 제목
    @Column(nullable = false)
    private String title;

    // 내용
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 모집 상태: 모집중 / 모집완료
    @Column(nullable = false)
    private String status;

    // 캠퍼스: 서울 / 글로벌
    @Column(nullable = false)
    private String campus;

    // ✅ 언어: 10개 중 하나만 저장
    @Column(nullable = false)
    private String language;

    // 최대 인원 (1~6)
    @Column(nullable = false)
    private Integer capacity;

    // 생성/수정 시간
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected StudyPost() {}

    public StudyPost(String title,
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

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ✅ 허용 언어 목록(한글명 10개)
    public static List<String> getAllowedLanguages() {
        return Arrays.asList(
                "영어", "일본어", "중국어", "독일어", "프랑스어",
                "스페인어", "이탈리아어", "러시아어", "베트남어", "한국어"
        );
    }

    // ===== Getter / Setter =====
    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCampus() { return campus; }
    public void setCampus(String campus) { this.campus = campus; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
