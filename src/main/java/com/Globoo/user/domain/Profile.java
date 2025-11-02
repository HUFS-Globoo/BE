// src/main/java/com/Globoo/user/domain/Profile.java
package com.Globoo.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "profiles",
        uniqueConstraints = @UniqueConstraint(name="uk_profiles_nickname", columnNames = "nickname"),
        indexes = @Index(name="idx_profiles_country", columnList = "country"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Profile {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // users.user_id UNIQUE FK
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_profiles_users"),
            unique = true)
    private User user;

    @Column(nullable = false, length = 30)
    private String nickname; // UNIQUE

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender; // ENUM('MALE','FEMALE','OTHER')

    @Enumerated(EnumType.STRING)
    private Campus campus; // ENUM('SEOUL','GLOBAL')

    @Column(length = 100)
    private String country; // DDL: VARCHAR(100)

    @Column(name = "profile_image", length = 255)
    private String profileImage;

    @Column(name = "info_title", length = 120)
    private String infoTitle;

    @Lob
    @Column(name = "info_content")
    private String infoContent;

    @Column(length = 8)
    private String mbti;

    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;
}
