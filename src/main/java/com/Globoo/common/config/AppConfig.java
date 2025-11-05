package com.Globoo.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // ✅ PartnerProfilePort 기본 구현 (매칭 시 상대방 프로필 카드 표시용)
   /* @Bean
   public MatchingService.PartnerProfilePort partnerProfilePort() {
    return (Long userId) -> new MatchingService.PartnerProfile(
                userId,
                "User" + userId,                 // 닉네임
                "Korean",                        // 사용 언어
                "English",                       // 선호 언어
                "KR",                            // 국적
                List.of("chat", "language-exchange"), // 키워드
                "/api/users/" + userId + "/profile-image" // 아바타 URL (임시)
        );
    }*/
}