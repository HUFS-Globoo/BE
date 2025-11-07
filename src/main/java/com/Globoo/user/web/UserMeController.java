// src/main/java/com/Globoo/user/web/UserMeController.java
package com.Globoo.user.web;

import com.Globoo.common.security.SecurityUtils;
import com.Globoo.user.dto.*;
import com.Globoo.user.service.UserMeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
@Tag(name = "MyPage")
public class UserMeController {

    private final UserMeService svc;

    // 프로필 이미지가 저장될 실제 디렉토리 (application.yml / application-prod.yml 에서 설정)
    @Value("${globoo.upload.profile-dir:/app/uploads/profile/}")
    private String profileUploadDir;

    @GetMapping
    @Operation(summary = "내 정보 조회")
    public MyPageRes me() {
        Long uid = SecurityUtils.requiredUserId();
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
    public void upload(@RequestPart("file") MultipartFile file) {
        Long uid = SecurityUtils.requiredUserId();

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일이 비어 있습니다.");
        }

        try {
            // 1) 업로드 디렉토리 보장
            Path uploadDir = Paths.get(profileUploadDir);
            Files.createDirectories(uploadDir);

            // 2) 파일명 생성 (UUID + 확장자)
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.lastIndexOf('.') != -1) {
                ext = originalName.substring(originalName.lastIndexOf('.')); // ".jpg" 등
            }
            String filename = UUID.randomUUID() + ext;

            // 3) 실제 저장
            Path dest = uploadDir.resolve(filename);
            file.transferTo(dest.toFile());

            // 4) DB에 저장할 URL (정적 리소스 매핑과 맞춰야 함)
            String url = "/uploads/profile/" + filename;

            svc.updateProfileImage(uid, url);

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "프로필 이미지 업로드에 실패했습니다.",
                    e
            );
        }
    }
}
