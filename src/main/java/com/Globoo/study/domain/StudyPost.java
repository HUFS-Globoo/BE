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

    /**
     * 주의: languages에는 언어 "code"를 저장한다. (예: en, ko, ru, pl ...)
     * DB languages 테이블의 PK(code)와 동일한 값을 저장하는 구조
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "study_post_languages", joinColumns = @JoinColumn(name = "study_post_id"))
    @Column(name = "language", nullable = false, length = 5)
    private Set<String> languages = new HashSet<>();

    @Column(nullable = false)
    private Integer capacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "studyPost", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<StudyMember> members = new HashSet<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected StudyPost() {}

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

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static List<String> getAllowedCampuses() {
        return Arrays.asList("서울", "글로벌");
    }

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

    public Set<StudyMember> getMembers() {
        return members;
    }
}
