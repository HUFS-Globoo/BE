package com.Globoo.study.domain;

import com.Globoo.user.domain.User;
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

    // ... (title, content, status, campuses, languages, capacity, user) ...
    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "study_post_campuses", joinColumns = @JoinColumn(name = "study_post_id"))
    @Column(name = "campus", nullable = false)
    private Set<String> campuses = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "study_post_languages", joinColumns = @JoinColumn(name = "study_post_id"))
    @Column(name = "language", nullable = false)
    private Set<String> languages = new HashSet<>();

    @Column(nullable = false)
    private Integer capacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ✅ (추가) 현재 가입한 멤버 목록
    // cascade = CascadeType.REMOVE: 스터디 글이 삭제되면 멤버 정보도 함께 삭제
    @OneToMany(mappedBy = "studyPost", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<StudyMember> members = new HashSet<>();

    // ... (createdAt, updatedAt) ...
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;


    protected StudyPost() {}

    // 생성자 수정 (user 파라미터는 이미 있음)
    public StudyPost(String title,
                     String content,
                     String status,
                     Set<String> campuses,
                     Set<String> languages,
                     Integer capacity,
                     User user) {
        this.title = title;
        this.content = content;
        this.status = status;
        this.campuses = campuses;
        this.languages = languages;
        this.capacity = capacity;
        this.user = user;
    }

    // ... (PrePersist, PreUpdate) ...
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ... (getAllowedLanguages, getAllowedCampuses) ...
    public static List<String> getAllowedLanguages() {
        return Arrays.asList(
                "영어", "일본어", "중국어", "독일어", "프랑스어",
                "스페인어", "이탈리아어", "러시아어", "베트남어", "한국어"
        );
    }
    public static List<String> getAllowedCampuses() {
        return Arrays.asList("서울", "글로벌");
    }

    // ===== Getter / Setter =====
    // ... (기존 Getter/Setter) ...
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

    // ✅ (추가) members Getter
    public Set<StudyMember> getMembers() {
        return members;
    }
}