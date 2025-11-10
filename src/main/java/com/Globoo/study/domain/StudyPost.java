package com.Globoo.study.domain;

import com.Globoo.user.domain.User; // User 엔티티 임포트
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    // 캠퍼스: 중복 저장
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "study_post_campuses", joinColumns = @JoinColumn(name = "study_post_id"))
    @Column(name = "campus", nullable = false)
    private Set<String> campuses = new HashSet<>();

    // 언어: 중복 저장
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "study_post_languages", joinColumns = @JoinColumn(name = "study_post_id"))
    @Column(name = "language", nullable = false)
    private Set<String> languages = new HashSet<>();

    // 최대 인원 (1~6)
    @Column(nullable = false)
    private Integer capacity;

    // 작성자 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 생성/수정 시간
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected StudyPost() {}

    // 생성자 수정
    public StudyPost(String title,
                     String content,
                     String status,
                     Set<String> campuses,
                     Set<String> languages,
                     Integer capacity,
                     User user) { // user 추가
        this.title = title;
        this.content = content;
        this.status = status;
        this.campuses = campuses;
        this.languages = languages;
        this.capacity = capacity;
        this.user = user; // user 할당
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

    // 허용 언어 목록
    public static List<String> getAllowedLanguages() {
        return Arrays.asList(
                "영어", "일본어", "중국어", "독일어", "프랑스어",
                "스페인어", "이탈리아어", "러시아어", "베트남어", "한국어"
        );
    }

    // 허용 캠퍼스 목록
    public static List<String> getAllowedCampuses() {
        return Arrays.asList("서울", "글로벌");
    }

    // ===== Getter / Setter =====
    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Set<String> getCampuses() { return campuses; }
    public void setCampuses(Set<String> campuses) { this.campuses = campuses; }

    public Set<String> getLanguages() { return languages; }
    public void setLanguages(Set<String> languages) { this.languages = languages; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public User getUser() { return user; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}