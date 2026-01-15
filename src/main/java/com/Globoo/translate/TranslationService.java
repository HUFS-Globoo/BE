package com.Globoo.translate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationService {

    private final RestTemplate restTemplate;

    @Value("${globoo.translation.deepl.url}")
    private String deeplUrl;

    @Value("${globoo.translation.deepl.auth-key}")
    private String deeplApiKey;

    @Value("${globoo.translation.libre.url}")
    private String libreUrl;

    /**
     * 번역 메인 메서드
     * @Cacheable: 동일한 text+targetLang 요청이 오면 메서드를 실행하지 않고 캐시된 값을 반환
     */
    @Cacheable(value = "translations", key = "#text + '_' + #targetLang")
    public TranslationDto.Response translate(String text, String targetLang) {

        // 빈 텍스트면 바로 반환
        if (text == null || text.trim().isEmpty()) {
            return new TranslationDto.Response(text, text, "NONE");
        }

        try {
            // 1. DeepL 시도
            String result = callDeepL(text, targetLang);
            return new TranslationDto.Response(text, result, "DeepL");

        } catch (HttpClientErrorException e) {
            // 456 에러(Quota Exceeded) 혹은 429(Too Many Requests) 발생 시
            if (e.getStatusCode().value() == 456 || e.getStatusCode().value() == 429) {
                log.warn("DeepL 사용량 초과! LibreTranslate로 전환합니다.");
            } else {
                log.error("DeepL API Error: {}", e.getMessage());
            }
            // 2. LibreTranslate 시도 (Fallback)
            return callLibre(text, targetLang);

        } catch (Exception e) {
            log.error("DeepL 알 수 없는 오류. Libre로 전환합니다.", e);
            return callLibre(text, targetLang);
        }
    }

    // --- Private Methods ---

    private String callDeepL(String text, String targetLang) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "DeepL-Auth-Key " + deeplApiKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("text", text);
        body.add("target_lang", targetLang.toUpperCase()); // EN, KO

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        TranslationDto.DeepLResponse response = restTemplate.postForObject(
                deeplUrl, request, TranslationDto.DeepLResponse.class
        );

        if (response != null && !response.getTranslations().isEmpty()) {
            return response.getTranslations().get(0).getText();
        }
        throw new RuntimeException("DeepL 응답 없음");
    }

    private TranslationDto.Response callLibre(String text, String targetLang) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("q", text);
            body.put("source", "auto"); // Libre는 auto 감지 지원
            body.put("target", targetLang.toLowerCase()); // en, ko
            body.put("format", "text");

            TranslationDto.LibreResponse response = restTemplate.postForObject(
                    libreUrl, body, TranslationDto.LibreResponse.class
            );

            if (response != null && response.getTranslatedText() != null) {
                return new TranslationDto.Response(text, response.getTranslatedText(), "LibreTranslate");
            }
        } catch (Exception e) {
            log.error("LibreTranslate 실패: {}", e.getMessage());
        }

        // 원문 반환
        return new TranslationDto.Response(text, text, "FAILED_BOTH");
    }
}