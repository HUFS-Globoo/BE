// src/main/java/com/Globoo/user/dto/KeywordRes.java
package com.Globoo.user.dto;

import com.Globoo.user.domain.Keyword;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KeywordRes {
    private Long id;
    private String name;
    private Keyword.Category category;
    private int sortOrder;

    public static KeywordRes from(Keyword k){
        return KeywordRes.builder()
                .id(k.getId())
                .name(k.getName())
                .category(k.getCategory())
                .sortOrder(k.getSortOrder())
                .build();
    }
}
