package com.Globoo.common.logging;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

@Service
public class LoggerService {

    public void logSignup(Long userId, String campus, LocalDate birthDate, Object gender) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("campus", campus);
        metadata.put("age_group", getAgeGroup(birthDate));
        metadata.put("gender", gender == null ? "unknown" : gender.toString());

        logEvent("USER_SIGNUP", userId, metadata);
    }

    public void logEvent(String eventName, Long userId) {
        logEvent(eventName, userId, new HashMap<>());
    }

    public void logEvent(String eventName, Long userId, Map<String, Object> metadata) {
        Map<String, Object> log = new HashMap<>();
        log.put("event_name", eventName);
        log.put("user_id", userId == null ? "unknown" : userId);
        log.put("timestamp", LocalDateTime.now().toString());
        log.put("status", "success");
        log.put("metadata", metadata == null ? new HashMap<>() : metadata);

        System.out.println(log);
    }

    public void logCommunityPostClick(Long userId, Long postId) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("post_id", postId);

        logEvent("COMMUNITY_POST_CLICK", userId, metadata);
    }

    private String getAgeGroup(LocalDate birthDate) {
        if (birthDate == null) return "unknown";

        int age = Period.between(birthDate, LocalDate.now()).getYears();

        if (age < 20) return "under_20";
        if (age < 25) return "20_24";
        if (age < 30) return "25_29";
        if (age < 35) return "30_34";
        return "35_plus";
    }
}