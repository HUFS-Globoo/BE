package com.Globoo.translate;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/translate")
@RequiredArgsConstructor
public class TranslationController {

    private final TranslationService translationService;

    @PostMapping
    public ResponseEntity<TranslationDto.Response> translateText(
            @RequestBody TranslationDto.Request request
    ) {

        TranslationDto.Response response = translationService.translate(
                request.getText(),
                request.getTargetLang()
        );

        return ResponseEntity.ok(response);
    }
}