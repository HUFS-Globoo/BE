// src/main/java/com/Globoo/user/domain/UserLanguage.java
package com.Globoo.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_languages",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_language",
                columnNames = {"user_id", "language_code", "type"}
        ),
        indexes = @Index(name = "idx_userlang_lang_type", columnList = "language_code, type")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserLanguage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK: users
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id",
            foreignKey = @ForeignKey(name = "fk_userlang_user"))
    private User user;

    // FK: languages(code)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "language_code",
            referencedColumnName = "code",
            foreignKey = @ForeignKey(name = "fk_userlang_language"))
    private Language language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private LanguageType type; // NATIVE or LEARN
}
