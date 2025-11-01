// src/main/java/com/Globoo/user/domain/UserKeyword.java
package com.Globoo.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_keywords",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_keyword",
                columnNames = {"user_id", "keyword_id"}
        ),
        indexes = {
                @Index(name="idx_userkeywords_user", columnList = "user_id"),
                @Index(name="idx_userkeywords_kw",   columnList = "keyword_id")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserKeyword {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK: users
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id",
            foreignKey = @ForeignKey(name = "fk_userkeywords_user"))
    private User user;

    // FK: keywords
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "keyword_id",
            foreignKey = @ForeignKey(name = "fk_userkeywords_keyword"))
    private Keyword keyword;
}
