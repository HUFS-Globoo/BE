// src/main/java/com/Globoo/user/dto/MyLanguagesRes.java
package com.Globoo.user.dto;

import lombok.*;
import java.util.List;

@Getter @Builder @AllArgsConstructor @NoArgsConstructor
public class MyLanguagesRes {
    private List<String> nativeCodes; // ["ko"]
    private List<String> learnCodes;  // ["en","ja"]
}
