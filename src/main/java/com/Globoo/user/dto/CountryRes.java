// src/main/java/com/Globoo/user/dto/CountryRes.java
package com.Globoo.user.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CountryRes {
    private String code;  // 예: "KR"
    private String name;  // 예: "대한민국"
}
