package com.Globoo.onboarding.service;

import com.Globoo.onboarding.dto.OnboardingStep3Req;
import com.Globoo.onboarding.dto.OnboardingStep4Req;
import com.Globoo.profile.store.ProfileRepository;
import com.Globoo.user.domain.*;
import com.Globoo.user.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final UserRepository userRepo;
    private final ProfileRepository profileRepo;
    private final UserLanguageRepository userLangRepo;
    private final LanguageRepository langRepo;
    private final UserKeywordRepository userKwRepo;
    private final KeywordRepository kwRepo;

    /** Step3: 국적 1개 + 언어(모국어 1개, 학습언어 1개) */
    @Transactional
    public void step3(Long userId, OnboardingStep3Req req) {
        // user/profile 존재 확인
        userRepo.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Profile p = profileRepo.findByUserId(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile not found"));

        // 1) 국적 저장 (null/blank 방어 + 대문자 정규화)
        String nat = req.nationalityCode();
        if (nat == null || nat.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "nationalityCode is required");
        }
        p.setCountry(nat.trim().toUpperCase());

        // 2) 언어 코드 검증
        Language nativeLang = langRepo.findById(req.nativeLanguageCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown native language code"));
        Language learnLang = langRepo.findById(req.preferredLanguageCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown learn language code"));

        // 3) 기존 언어 삭제 후 2개 저장
        userLangRepo.deleteAllByUserId(userId);

        User userRef = User.builder().id(userId).build();

        userLangRepo.save(UserLanguage.builder()
                .user(userRef)
                .language(nativeLang)
                .type(LanguageType.NATIVE)
                .build());

        userLangRepo.save(UserLanguage.builder()
                .user(userRef)
                .language(learnLang)
                .type(LanguageType.LEARN)
                .build());
    }

    /** Step4: MBTI + 키워드(각 카테고리 3~5개) */
    @Transactional
    public void step4(Long userId, OnboardingStep4Req req) {
        Profile p = profileRepo.findByUserId(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile not found"));

        // 1) MBTI 저장
        p.setMbti(req.mbti());

        // 2) 키워드 중복 제거 + 개수 검증(3~5)
        List<String> pks = dedup(req.personalityKeywords());
        List<String> hks = dedup(req.hobbyKeywords());
        List<String> tks = dedup(req.topicKeywords());

        validateKeywordCount(pks);
        validateKeywordCount(hks);
        validateKeywordCount(tks);

        // 3) 키워드 존재 검증 (category+nameIn)
        List<Keyword> kp = kwRepo.findAllByCategoryAndNameIn(Keyword.Category.PERSONALITY, pks);
        if (kp.size() != pks.size())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown PERSONALITY keywords");

        List<Keyword> kh = kwRepo.findAllByCategoryAndNameIn(Keyword.Category.HOBBY, hks);
        if (kh.size() != hks.size())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown HOBBY keywords");

        List<Keyword> kt = kwRepo.findAllByCategoryAndNameIn(Keyword.Category.TOPIC, tks);
        if (kt.size() != tks.size())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown TOPIC keywords");

        // 4) 기존 매핑 삭제 후 저장
        userKwRepo.deleteAllByUser_Id(userId);


        User userRef = User.builder().id(userId).build();

        for (Keyword k : kp) userKwRepo.save(UserKeyword.builder().user(userRef).keyword(k).build());
        for (Keyword k : kh) userKwRepo.save(UserKeyword.builder().user(userRef).keyword(k).build());
        for (Keyword k : kt) userKwRepo.save(UserKeyword.builder().user(userRef).keyword(k).build());
    }

    private List<String> dedup(List<String> in) {
        if (in == null) return List.of();
        return new ArrayList<>(new LinkedHashSet<>(in));
    }

    private void validateKeywordCount(List<String> keywords) {
        int size = keywords.size();
        if (size < 3 || size > 5) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "각 키워드는 최소 3개에서 5개까지 선택해야 합니다."
            );
        }
    }
}
