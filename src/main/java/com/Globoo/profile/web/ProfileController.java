// src/main/java/com/Globoo/profile/web/ProfileController.java
package com.Globoo.profile.web;

import com.Globoo.common.security.SecurityUtils;
import com.Globoo.profile.dto.ProfileCardRes;
import com.Globoo.profile.dto.ProfileDetailRes;
import com.Globoo.profile.service.ProfileService;
import com.Globoo.user.domain.Campus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@Tag(name = "Profiles")
public class ProfileController {

    private final ProfileService svc;

    @GetMapping("/{id}")
    @Operation(summary = "프로필 상세 조회")
    public ProfileDetailRes detail(@PathVariable Long id) {
        Long viewerId = SecurityUtils.currentUserId();
        return svc.getDetail(id, viewerId); // id는 userId 또는 profileId 어느 쪽이든 OK
    }

    @GetMapping
    @Operation(summary = "프로필 목록 조회(필터/페이지)")
    public Page<ProfileCardRes> list(
            @RequestParam(required = false) Campus campus,
            @RequestParam(required = false) String nativeLang,
            @RequestParam(required = false) String learnLang,
            @RequestParam(required = false, name = "keywordId") List<Long> keywordIds,
            @ParameterObject Pageable pageable
    ) {
        Pageable capped = PageRequest.of(
                pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), 50),
                pageable.getSort().isSorted() ? pageable.getSort()
                        : Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return svc.search(campus, nativeLang, learnLang, keywordIds, capped);
    }
}
