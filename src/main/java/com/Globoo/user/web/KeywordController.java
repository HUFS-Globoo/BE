// src/main/java/com/Globoo/user/web/KeywordController.java
package com.Globoo.user.web;

import com.Globoo.user.domain.Keyword;
import com.Globoo.user.dto.KeywordGroupRes;
import com.Globoo.user.dto.KeywordRes;
import com.Globoo.user.service.KeywordQueryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/keywords")
@RequiredArgsConstructor
@Tag(name = "Keywords", description = "키워드 조회")
public class KeywordController {

    private final KeywordQueryService svc;

    @GetMapping
    @Operation(summary = "전체 키워드(활성) 목록", description = "카테고리/정렬 순으로 반환")
    public List<KeywordRes> listAll() {
        return svc.listAllActive();
    }

    @GetMapping("/grouped")
    @Operation(summary = "카테고리별 그룹 반환", description = "personality/hobby/topic 필드로 그룹핑")
    public KeywordGroupRes listGrouped() {
        return svc.listGrouped();
    }

    @GetMapping("/{category}")
    @Operation(summary = "특정 카테고리 키워드", description = "category = PERSONALITY | HOBBY | TOPIC")
    public List<KeywordRes> listByCategory(@PathVariable("category") Keyword.Category category) {
        return svc.listByCategory(category);
    }
}
