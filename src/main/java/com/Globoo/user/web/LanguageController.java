// src/main/java/com/Globoo/user/web/LanguageController.java
package com.Globoo.user.web;

import com.Globoo.user.dto.LanguageRes;
import com.Globoo.user.repository.LanguageRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/languages")
@RequiredArgsConstructor
@Tag(name="Languages", description="언어 조회")
public class LanguageController {
    private final LanguageRepository repo;

    @GetMapping
    public java.util.List<LanguageRes> listAll(){
        return repo.findAll().stream()
                .map(l -> new LanguageRes(l.getCode(), l.getName()))
                .toList();
    }
}
