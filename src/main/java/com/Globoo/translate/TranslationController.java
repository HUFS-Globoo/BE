package com.Globoo.translate;

import com.Globoo.common.logging.LoggerService;
import com.Globoo.common.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/translate")
@RequiredArgsConstructor
public class TranslationController {

    private final TranslationService translationService;
    private final LoggerService loggerService;

    @PostMapping
    public ResponseEntity<TranslationDto.Response> translateText(
            @RequestBody TranslationDto.Request request
    ) {
        Long userId = SecurityUtils.requiredUserId();

        loggerService.logEvent("TRANSLATION_USED", userId);

        TranslationDto.Response response = translationService.translate(
                request.getText(),
                request.getTargetLang()
        );

        return ResponseEntity.ok(response);
    }
}