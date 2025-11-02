// src/main/java/com/Globoo/user/domain/CountryList.java
package com.Globoo.user.domain;

import com.Globoo.user.dto.CountryRes;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum CountryList {
    KR("대한민국"),
    US("미국"),
    JP("일본"),
    CN("중국"),
    FR("프랑스"),
    DE("독일"),
    UK("영국"),
    CA("캐나다"),
    AU("호주");

    private final String name;
    CountryList(String name) { this.name = name; }

    public static List<CountryRes> toList() {
        return Arrays.stream(values())
                .map(c -> new CountryRes(c.name(), c.getName()))
                .toList();
    }
}
