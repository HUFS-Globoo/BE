package com.Globoo.common.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

@Service
public class LoggerService {

    private static final String EVENT_LOG_PREFIX = "[GLOBOO_EVENT] ";

    private final ObjectMapper objectMapper;

    public LoggerService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

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

        try {
            System.out.println(EVENT_LOG_PREFIX + objectMapper.writeValueAsString(log));
        } catch (Exception e) {
            System.out.println(EVENT_LOG_PREFIX + "{\"event_name\":\"LOGGING_FAILED\",\"status\":\"error\"}");
        }
    }

    private String getAgeGroup(LocalDate birthDate) {
        if (birthDate == null) return "unknown";

        int age = Period.between(birthDate, LocalDate.now()).getYears();

        if (age < 20) return "under_20";
        if (age < 25) return "20_24";
        if (age < 30) return "25_29";
        if (age < 35) return "30_34";
        return "35_plus"; //★ 나이대 -> 학년? 으로 구성할건지 아니면 그냥 나이대로 구성할건지에 대한 회의가 조금은 필요해보임!
    }
}