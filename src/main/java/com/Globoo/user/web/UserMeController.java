// src/main/java/com/Globoo/user/web/UserMeController.java
package com.Globoo.user.web;

import com.Globoo.common.security.SecurityUtils;   // 여기!
import com.Globoo.user.dto.*;
import com.Globoo.user.service.UserMeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
@Tag(name = "MyPage")
public class UserMeController {

    private final UserMeService svc;

    @GetMapping
    @Operation(summary = "내 정보 조회")
    public MyPageRes me() {
        Long uid = SecurityUtils.requiredUserId();  //  Sec → SecurityUtils
        return svc.getMyPage(uid);
    }

    @PatchMapping
    @Operation(summary = "내 정보 수정(1:1 필드)")
    public void update(@RequestBody ProfileUpdateReq req) {
        Long uid = SecurityUtils.requiredUserId();
        svc.updateProfile(uid, req);
    }

    @GetMapping("/languages")
    @Operation(summary = "내 언어 조회")
    public MyLanguagesRes myLangs() {
        Long uid = SecurityUtils.requiredUserId();
        return svc.getMyLanguages(uid);
    }

    @PutMapping("/languages")
    @Operation(summary = "내 언어 수정")
    public void putLangs(@RequestBody MyLanguagesUpdateReq req) {
        Long uid = SecurityUtils.requiredUserId();
        svc.updateMyLanguages(uid, req);
    }

    @GetMapping("/keywords")
    @Operation(summary = "내 키워드 조회")
    public MyKeywordsRes myKeywords() {
        Long uid = SecurityUtils.requiredUserId();
        return svc.getMyKeywords(uid);
    }

    @PutMapping("/keywords")
    @Operation(summary = "내 키워드 수정")
    public void putKeywords(@RequestBody MyKeywordsUpdateReq req) {
        Long uid = SecurityUtils.requiredUserId();
        svc.updateMyKeywords(uid, req);
    }

    @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로필 이미지 업로드")
    public void upload(@RequestPart("file") MultipartFile file) throws Exception {
        Long uid = SecurityUtils.requiredUserId();
        // 샘플: 로컬 저장 (운영은 S3 등으로 교체)
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        java.nio.file.Path dest = java.nio.file.Paths.get("uploads").resolve(filename);
        java.nio.file.Files.createDirectories(dest.getParent());
        file.transferTo(dest.toFile());
        String url = "/uploads/" + filename; // 정적 서빙 매핑 필요(스프링 리소스 핸들러)
        svc.updateProfileImage(uid, url);
    }
}
