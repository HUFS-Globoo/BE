// src/main/java/com/Globoo/user/dto/MyLanguagesUpdateReq.java
package com.Globoo.user.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class MyLanguagesUpdateReq {
    private List<String> nativeCodes;
    private List<String> learnCodes;
}
