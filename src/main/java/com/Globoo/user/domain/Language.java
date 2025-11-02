// src/main/java/com/Globoo/user/domain/Language.java
package com.Globoo.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "languages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Language {

    @Id
    @Column(length = 5) // ISO 639-1
    private String code; // PK

    @Column(nullable = false, length = 50, unique = true)
    private String name;
}
