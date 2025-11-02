// src/main/java/com/Globoo/user/dto/ProfileUpdateReq.java
package com.Globoo.user.dto;

import com.Globoo.user.domain.Campus;
import lombok.Getter;

@Getter
public class ProfileUpdateReq {
    private String nickname;     // 선택적
    private String infoTitle;    // 선택적
    private String infoContent;  // 선택적
    private String mbti;         // 선택적
    private Campus campus;       // 선택적 (SEOUL/GLOBAL)
    private String country;      // 선택적 (ex: "KR")
}
