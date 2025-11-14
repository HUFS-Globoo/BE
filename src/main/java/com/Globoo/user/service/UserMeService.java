package com.Globoo.user.service;

import com.Globoo.profile.store.ProfileRepository;
import com.Globoo.user.domain.*;
import com.Globoo.user.dto.*;
import com.Globoo.user.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserMeService {

    private final UserRepository userRepo;
    private final ProfileRepository profileRepo;
    private final UserLanguageRepository userLangRepo;
    private final LanguageRepository langRepo;
    private final UserKeywordRepository userKwRepo;
    private final KeywordRepository kwRepo;

    @Transactional(readOnly = true)
    public MyPageRes getMyPage(Long userId) {
        User u = userRepo.findById(userId).orElseThrow();
        Profile p = profileRepo.findByUserId(userId).orElseThrow();

        List<UserLanguage> uls = userLangRepo.findAllByUserId(userId);
        List<String> natives = uls.stream()
                .filter(x -> x.getType() == LanguageType.NATIVE)
                .map(x -> x.getLanguage().getCode())
                .toList();
        List<String> learns = uls.stream()
                .filter(x -> x.getType() == LanguageType.LEARN)
                .map(x -> x.getLanguage().getCode())
                .toList();

        List<UserKeyword> uks = userKwRepo.findAllByUserId(userId);
        List<String> personality = uks.stream()
                .filter(k -> k.getKeyword().getCategory() == Keyword.Category.PERSONALITY)
                .map(k -> k.getKeyword().getName())
                .toList();
        List<String> hobby = uks.stream()
                .filter(k -> k.getKeyword().getCategory() == Keyword.Category.HOBBY)
                .map(k -> k.getKeyword().getName())
                .toList();
        List<String> topic = uks.stream()
                .filter(k -> k.getKeyword().getCategory() == Keyword.Category.TOPIC)
                .map(k -> k.getKeyword().getName())
                .toList();

        //프로필 이미지 URL 전처리 (앞 슬래시 제거)
        String imageUrl = p.getProfileImage();
        if (imageUrl != null && imageUrl.startsWith("/")) {
            imageUrl = imageUrl.substring(1); // "/uploads/..." → "uploads/..."
        }

        return MyPageRes.builder()
                .name(u.getName())
                .nickname(p.getNickname())
                .mbti(p.getMbti())
                .profileImageUrl(imageUrl) // 수정된 이미지 URL 적용
                .infoTitle(p.getInfoTitle())
                .infoContent(p.getInfoContent())
                .campus(p.getCampus())
                .country(p.getCountry())
                .email(u.getEmail())
                .nativeLanguages(natives)
                .learnLanguages(learns)
                .personalityKeywords(personality)
                .hobbyKeywords(hobby)
                .topicKeywords(topic)
                .build();
    }

    @Transactional
    public void updateProfile(Long userId, ProfileUpdateReq req) {
        Profile p = profileRepo.findByUserId(userId).orElseThrow();

        if (req.getNickname() != null) p.setNickname(req.getNickname());
        if (req.getInfoTitle() != null) p.setInfoTitle(req.getInfoTitle());
        if (req.getInfoContent() != null) p.setInfoContent(req.getInfoContent());
        if (req.getMbti() != null) p.setMbti(req.getMbti());
        if (req.getCampus() != null) p.setCampus(req.getCampus());
        if (req.getCountry() != null) p.setCountry(req.getCountry());
    }

    @Transactional(readOnly = true)
    public MyLanguagesRes getMyLanguages(Long userId) {
        List<UserLanguage> uls = userLangRepo.findAllByUserId(userId);
        return MyLanguagesRes.builder()
                .nativeCodes(uls.stream()
                        .filter(x -> x.getType() == LanguageType.NATIVE)
                        .map(x -> x.getLanguage().getCode())
                        .toList())
                .learnCodes(uls.stream()
                        .filter(x -> x.getType() == LanguageType.LEARN)
                        .map(x -> x.getLanguage().getCode())
                        .toList())
                .build();
    }

    @Transactional
    public void updateMyLanguages(Long userId, MyLanguagesUpdateReq req) {
        // 요청 중복 제거 (같은 코드 여러 번 와도 한 번만 처리)
        Set<String> natives = new HashSet<>(Optional.ofNullable(req.getNativeCodes()).orElseGet(List::of));
        Set<String> learns = new HashSet<>(Optional.ofNullable(req.getLearnCodes()).orElseGet(List::of));

        // 1) 둘 다 비어 있으면 → 해당 유저 언어 전체 삭제 후 종료
        if (natives.isEmpty() && learns.isEmpty()) {
            userLangRepo.deleteAllByUserId(userId);
            return;
        }

        // 2) 네이티브 언어 코드 검증
        Map<String, Language> byCode = langRepo.findAllById(new ArrayList<>(natives)).stream()
                .collect(Collectors.toMap(Language::getCode, l -> l));
        if (byCode.size() != natives.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown native language code");
        }

        // 3) 학습 언어 코드 검증
        Map<String, Language> byCode2 = langRepo.findAllById(new ArrayList<>(learns)).stream()
                .collect(Collectors.toMap(Language::getCode, l -> l));
        if (byCode2.size() != learns.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown learn language code");
        }

        // 4) 기존 데이터 싹 삭제 (bulk delete)
        userLangRepo.deleteAllByUserId(userId);

        // 5) 새로 저장
        User userRef = User.builder().id(userId).build();

        for (String c : natives) {
            userLangRepo.save(UserLanguage.builder()
                    .user(userRef)
                    .language(byCode.get(c))
                    .type(LanguageType.NATIVE)
                    .build());
        }
        for (String c : learns) {
            userLangRepo.save(UserLanguage.builder()
                    .user(userRef)
                    .language(byCode2.get(c))
                    .type(LanguageType.LEARN)
                    .build());
        }
    }

    @Transactional(readOnly = true)
    public MyKeywordsRes getMyKeywords(Long userId) {
        List<UserKeyword> uks = userKwRepo.findAllByUserId(userId);
        return MyKeywordsRes.builder()
                .personality(uks.stream()
                        .filter(k -> k.getKeyword().getCategory() == Keyword.Category.PERSONALITY)
                        .map(k -> k.getKeyword().getName())
                        .toList())
                .hobby(uks.stream()
                        .filter(k -> k.getKeyword().getCategory() == Keyword.Category.HOBBY)
                        .map(k -> k.getKeyword().getName())
                        .toList())
                .topic(uks.stream()
                        .filter(k -> k.getKeyword().getCategory() == Keyword.Category.TOPIC)
                        .map(k -> k.getKeyword().getName())
                        .toList())
                .build();
    }

    @Transactional
    public void updateMyKeywords(Long userId, MyKeywordsUpdateReq req) {
        List<String> p = Optional.ofNullable(req.getPersonality()).orElseGet(List::of);
        List<String> h = Optional.ofNullable(req.getHobby()).orElseGet(List::of);
        List<String> t = Optional.ofNullable(req.getTopic()).orElseGet(List::of);

        List<Keyword> kp = kwRepo.findAllByCategoryAndNameIn(Keyword.Category.PERSONALITY, p);
        if (kp.size() != p.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown PERSONALITY keywords");
        }

        List<Keyword> kh = kwRepo.findAllByCategoryAndNameIn(Keyword.Category.HOBBY, h);
        if (kh.size() != h.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown HOBBY keywords");
        }

        List<Keyword> kt = kwRepo.findAllByCategoryAndNameIn(Keyword.Category.TOPIC, t);
        if (kt.size() != t.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown TOPIC keywords");
        }

        userKwRepo.deleteByUserId(userId);
        for (Keyword k : kp) {
            userKwRepo.save(UserKeyword.builder()
                    .user(User.builder().id(userId).build())
                    .keyword(k)
                    .build());
        }
        for (Keyword k : kh) {
            userKwRepo.save(UserKeyword.builder()
                    .user(User.builder().id(userId).build())
                    .keyword(k)
                    .build());
        }
        for (Keyword k : kt) {
            userKwRepo.save(UserKeyword.builder()
                    .user(User.builder().id(userId).build())
                    .keyword(k)
                    .build());
        }
    }

    @Transactional
    public void updateProfileImage(Long userId, String imageUrl) {
        Profile p = profileRepo.findByUserId(userId).orElseThrow();
        p.setProfileImage(imageUrl); // 실제 파일 업로드는 Controller에서 처리
    }
}
