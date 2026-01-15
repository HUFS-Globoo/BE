package com.Globoo.translate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public class TranslationDto {

    // Request
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Request {
        private String text;       // 번역할 문장
        private String targetLang; // 목표 언어 (EN, KO)
    }

    // Response
    @Getter @AllArgsConstructor
    public static class Response {
        private String originalText;
        private String translatedText;
        private String serviceUsed;
    }


    // DeepL API 응답 구조
    @Getter @NoArgsConstructor
    public static class DeepLResponse {
        private List<Translation> translations;
        @Getter @NoArgsConstructor
        public static class Translation {
            private String text;
            private String detected_source_language;
        }
    }

    // LibreTranslate API 응답 구조
    @Getter @NoArgsConstructor
    public static class LibreResponse {
        private String translatedText;
    }
}