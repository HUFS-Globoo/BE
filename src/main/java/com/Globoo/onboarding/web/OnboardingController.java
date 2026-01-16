package com.Globoo.onboarding.web;

import com.Globoo.auth.dto.OkRes;
import com.Globoo.common.security.SecurityUtils;
import com.Globoo.onboarding.dto.OnboardingStep3Req;
import com.Globoo.onboarding.dto.OnboardingStep4Req;
import com.Globoo.onboarding.service.OnboardingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
@Tag(name="Onboarding")
public class OnboardingController {

    private final OnboardingService svc;

    @PostMapping("/step3")
    public OkRes step3(@Valid @RequestBody OnboardingStep3Req req) {
        Long uid = SecurityUtils.requiredUserId();
        svc.step3(uid, req);
        return new OkRes(true);
    }

    @PostMapping("/step4")
    public OkRes step4(@Valid @RequestBody OnboardingStep4Req req) {
        Long uid = SecurityUtils.requiredUserId();
        svc.step4(uid, req);
        return new OkRes(true); // 프론트: 가입 완료 → 로그인 페이지로 이동
    }
}
