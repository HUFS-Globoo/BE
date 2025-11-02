// src/main/java/com/Globoo/user/dto/MyKeywordsUpdateReq.java
package com.Globoo.user.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class MyKeywordsUpdateReq {
    private List<String> personality; // 키워드 name 배열
    private List<String> hobby;
    private List<String> topic;
}
