package com.Globoo.user.service;

import com.Globoo.profile.store.ProfileRepository;
import com.Globoo.user.domain.*;
import com.Globoo.user.dto.*;
import com.Globoo.user.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    // 프로필 이미지 실제 저장 경로 (UserMeController 업로드 경로와 동일해야 함)
    @Value("${globoo.upload.profile-dir:/home/ubuntu/app/uploads/profile/}")
    private String profileUploadDir;

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

        // 프로필 이미지 URL 전처리 (앞 슬래시 제거)
        String imageUrl = p.getProfileImage();
        if (imageUrl != null && imageUrl.startsWith("/")) {
            imageUrl = imageUrl.substring(1);
        }

        // country null 방어 + 정규화
        String country = p.getCountry();
        if (country == null || country.isBlank()) {
            country = "KR"; // 기본값 정책
        } else {
            country = country.trim().toUpperCase();
        }

        return MyPageRes.builder()
                .name(u.getName())
                .nickname(p.getNickname())
                .mbti(p.getMbti())
                .profileImageUrl(imageUrl)
                .infoTitle(p.getInfoTitle())
                .infoContent(p.getInfoContent())
                .campus(p.getCampus())
                .country(country)
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

        // country 입력 보정 (빈문자/소문자 대응)
        if (req.getCountry() != null) {
            String c = req.getCountry().trim();
            if (c.isEmpty()) {
                p.setCountry(null);
            } else {
                p.setCountry(c.toUpperCase());
            }
        }
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
        Set<String> natives = new HashSet<>(Optional.ofNullable(req.getNativeCodes()).orElseGet(List::of));
        Set<String> learns  = new HashSet<>(Optional.ofNullable(req.getLearnCodes()).orElseGet(List::of));

        if (natives.isEmpty() && learns.isEmpty()) {
            userLangRepo.deleteAllByUserId(userId);
            return;
        }

        Map<String, Language> byCode = langRepo.findAllById(new ArrayList<>(natives)).stream()
                .collect(Collectors.toMap(Language::getCode, l -> l));
        if (byCode.size() != natives.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown native language code");
        }

        Map<String, Language> byCode2 = langRepo.findAllById(new ArrayList<>(learns)).stream()
                .collect(Collectors.toMap(Language::getCode, l -> l));
        if (byCode2.size() != learns.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown learn language code");
        }

        userLangRepo.deleteAllByUserId(userId);

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
        List<String> p = new ArrayList<>(new LinkedHashSet<>(Optional.ofNullable(req.getPersonality()).orElseGet(List::of)));
        List<String> h = new ArrayList<>(new LinkedHashSet<>(Optional.ofNullable(req.getHobby()).orElseGet(List::of)));
        List<String> t = new ArrayList<>(new LinkedHashSet<>(Optional.ofNullable(req.getTopic()).orElseGet(List::of)));

        validateKeywordCount(p);
        validateKeywordCount(h);
        validateKeywordCount(t);

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

        User userRef = User.builder().id(userId).build();

        for (Keyword k : kp) userKwRepo.save(UserKeyword.builder().user(userRef).keyword(k).build());
        for (Keyword k : kh) userKwRepo.save(UserKeyword.builder().user(userRef).keyword(k).build());
        for (Keyword k : kt) userKwRepo.save(UserKeyword.builder().user(userRef).keyword(k).build());
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

    @Transactional
    public void updateProfileImage(Long userId, String imageUrl) {
        Profile p = profileRepo.findByUserId(userId).orElseThrow();
        p.setProfileImage(imageUrl);
    }

    /**
     * 업로드 프로필 이미지를 삭제하고 기본(국적) 이미지로 리셋
     * - DB: profiles.profile_image = null
     * - 파일: EC2 디스크에서 삭제(가능하면)
     */
    @Transactional
    public void deleteProfileImage(Long userId) {
        Profile p = profileRepo.findByUserId(userId).orElseThrow();

        String imageUrl = p.getProfileImage(); // 예: "uploads/profile/xxx.svg"
        p.setProfileImage(null); // DB 먼저 리셋 (UX 안전)

        if (imageUrl == null || imageUrl.isBlank()) return;

        // 허용된 경로만 삭제 시도 (안전장치)
        String prefix = "uploads/profile/";
        if (!imageUrl.startsWith(prefix)) return;

        String filename = imageUrl.substring(prefix.length());
        if (filename.isBlank()) return;

        String dir = profileUploadDir.endsWith("/") ? profileUploadDir : profileUploadDir + "/";

        Path baseDir = Paths.get(dir).normalize();
        Path filePath = baseDir.resolve(filename).normalize();

        // 디렉토리 밖으로 나가는 경로 공격 방지
        if (!filePath.startsWith(baseDir)) return;

        try {
            Files.deleteIfExists(filePath);
        } catch (Exception ignored) {
            // 파일 삭제 실패해도 DB는 이미 null이라 기능은 정상
        }
    }

    @Transactional
    public void withdraw(Long userId) {
        if (!userRepo.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepo.deleteById(userId);
    }
}
