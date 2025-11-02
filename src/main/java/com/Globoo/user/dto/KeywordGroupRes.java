// src/main/java/com/Globoo/user/dto/KeywordGroupRes.java
package com.Globoo.user.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KeywordGroupRes {
    private List<KeywordRes> personality;
    private List<KeywordRes> hobby;
    private List<KeywordRes> topic;
}
