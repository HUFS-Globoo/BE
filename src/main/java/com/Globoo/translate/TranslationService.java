package com.Globoo.translate;

import com.Globoo.common.logging.LoggerService;
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
    private final LoggerService loggerService;

    @Value("${globoo.translation.deepl.url}")
    private String deeplUrl;

    @Value("${globoo.translation.deepl.auth-key}")
    private String deeplApiKey;

    @Value("${globoo.translation.libre.url}")
    private String libreUrl;

    @Cacheable(value = "translations", key = "#text + '_' + #targetLang")
    public TranslationDto.Response translate(String text, String targetLang) {

        if (text == null || text.trim().isEmpty()) {
            return new TranslationDto.Response(text, text, "NONE");
        }

        try {
            String result = callDeepL(text, targetLang);

            loggerService.logEvent("TRANSLATION_USED", null);

            return new TranslationDto.Response(text, result, "DeepL");

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 456 || e.getStatusCode().value() == 429) {
                log.warn("DeepL 사용량 초과! LibreTranslate로 전환합니다.");
            } else {
                log.error("DeepL API Error: {}", e.getMessage());
            }

            TranslationDto.Response response = callLibre(text, targetLang);

            loggerService.logEvent("TRANSLATION_USED", null);

            return response;

        } catch (Exception e) {
            log.error("DeepL 알 수 없는 오류. Libre로 전환합니다.", e);

            TranslationDto.Response response = callLibre(text, targetLang);

            loggerService.logEvent("TRANSLATION_USED", null);

            return response;
        }
    }

    private String callDeepL(String text, String targetLang) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "DeepL-Auth-Key " + deeplApiKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("text", text);
        body.add("target_lang", targetLang.toUpperCase());

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
            body.put("source", "auto");
            body.put("target", targetLang.toLowerCase());
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

        return new TranslationDto.Response(text, text, "FAILED_BOTH");
    }
}