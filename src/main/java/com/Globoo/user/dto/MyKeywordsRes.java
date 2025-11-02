// src/main/java/com/Globoo/user/dto/MyKeywordsRes.java
package com.Globoo.user.dto;

import lombok.*;
import java.util.List;

@Getter @Builder @AllArgsConstructor @NoArgsConstructor
public class MyKeywordsRes {
    private List<String> personality;
    private List<String> hobby;
    private List<String> topic;
}
