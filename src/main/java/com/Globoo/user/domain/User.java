// src/main/java/com/Globoo/user/domain/User.java
package com.Globoo.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_username", columnNames = "username")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=254)
    private String email;

    @Column(nullable=false, unique=true, length=30)
    private String username;   // ← 너의 기준 필드(두 번째 코드의 nickname 대체)

    @Column(nullable=false, length=100)
    private String password;

    @Column(nullable=false, length=50)
    private String name;

    @Column(name="phone_number", length=20)
    private String phoneNumber;

    // 선택 필드: 두 번째 코드의 profileImageUrl 흡수 (nullable 허용)
    @Column(name="profile_image_url", length=512)
    private String profileImageUrl;

    @Column(name="is_school_verified", nullable=false)
    private boolean schoolVerified;

    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;

    // 프로필 1:1
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = true)
    private Profile profile;

    // 사용 언어
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @OrderBy("id ASC")
    private Set<UserLanguage> userLanguages = new LinkedHashSet<>();

    // 키워드
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @OrderBy("id ASC")
    private Set<UserKeyword> userKeywords = new LinkedHashSet<>();
}
