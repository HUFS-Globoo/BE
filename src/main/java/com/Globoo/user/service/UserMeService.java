// src/main/java/com/Globoo/user/service/UserMeService.java
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
        List<String> personality = uks.stream().filter(k -> k.getKeyword().getCategory() == Keyword.Category.PERSONALITY)
                .map(k -> k.getKeyword().getName()).toList();
        List<String> hobby = uks.stream().filter(k -> k.getKeyword().getCategory() == Keyword.Category.HOBBY)
                .map(k -> k.getKeyword().getName()).toList();
        List<String> topic = uks.stream().filter(k -> k.getKeyword().getCategory() == Keyword.Category.TOPIC)
                .map(k -> k.getKeyword().getName()).toList();

        return MyPageRes.builder()
                .name(u.getName())
                .nickname(p.getNickname())
                .mbti(p.getMbti())
                .profileImageUrl(p.getProfileImage()) // null이면 프론트 기본 이미지 사용
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
                .nativeCodes(uls.stream().filter(x -> x.getType()==LanguageType.NATIVE).map(x->x.getLanguage().getCode()).toList())
                .learnCodes(uls.stream().filter(x -> x.getType()==LanguageType.LEARN).map(x->x.getLanguage().getCode()).toList())
                .build();
    }

    @Transactional
    public void updateMyLanguages(Long userId, MyLanguagesUpdateReq req) {
        Set<String> natives = new HashSet<>(Optional.ofNullable(req.getNativeCodes()).orElseGet(List::of));
        Set<String> learns  = new HashSet<>(Optional.ofNullable(req.getLearnCodes()).orElseGet(List::of));

        // 존재 검증
        Map<String, Language> byCode = langRepo.findAllById(
                        new ArrayList<>(Set.copyOf(natives))).stream()
                .collect(Collectors.toMap(Language::getCode, l -> l));
        if (byCode.size() != natives.size())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown native language code");

        Map<String, Language> byCode2 = langRepo.findAllById(
                        new ArrayList<>(Set.copyOf(learns))).stream()
                .collect(Collectors.toMap(Language::getCode, l -> l));
        if (byCode2.size() != learns.size())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown learn language code");

        // 전체 재저장(간단/안전)
        userLangRepo.deleteByUserId(userId);
        for (String c : natives) {
            userLangRepo.save(UserLanguage.builder()
                    .user(User.builder().id(userId).build())
                    .language(byCode.get(c))
                    .type(LanguageType.NATIVE).build());
        }
        for (String c : learns) {
            userLangRepo.save(UserLanguage.builder()
                    .user(User.builder().id(userId).build())
                    .language(byCode2.get(c))
                    .type(LanguageType.LEARN).build());
        }
    }

    @Transactional(readOnly = true)
    public MyKeywordsRes getMyKeywords(Long userId) {
        List<UserKeyword> uks = userKwRepo.findAllByUserId(userId);
        return MyKeywordsRes.builder()
                .personality(uks.stream().filter(k->k.getKeyword().getCategory()== Keyword.Category.PERSONALITY).map(k->k.getKeyword().getName()).toList())
                .hobby(uks.stream().filter(k->k.getKeyword().getCategory()== Keyword.Category.HOBBY).map(k->k.getKeyword().getName()).toList())
                .topic(uks.stream().filter(k->k.getKeyword().getCategory()== Keyword.Category.TOPIC).map(k->k.getKeyword().getName()).toList())
                .build();
    }

    @Transactional
    public void updateMyKeywords(Long userId, MyKeywordsUpdateReq req) {
        List<String> p = Optional.ofNullable(req.getPersonality()).orElseGet(List::of);
        List<String> h = Optional.ofNullable(req.getHobby()).orElseGet(List::of);
        List<String> t = Optional.ofNullable(req.getTopic()).orElseGet(List::of);

        List<Keyword> kp = kwRepo.findAllByCategoryAndNameIn(Keyword.Category.PERSONALITY, p);
        if (kp.size()!=p.size())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown PERSONALITY keywords");

        List<Keyword> kh = kwRepo.findAllByCategoryAndNameIn(Keyword.Category.HOBBY, h);
        if (kh.size()!=h.size())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown HOBBY keywords");

        List<Keyword> kt = kwRepo.findAllByCategoryAndNameIn(Keyword.Category.TOPIC, t);
        if (kt.size()!=t.size())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown TOPIC keywords");

        userKwRepo.deleteByUserId(userId);
        for (Keyword k : kp) userKwRepo.save(UserKeyword.builder().user(User.builder().id(userId).build()).keyword(k).build());
        for (Keyword k : kh) userKwRepo.save(UserKeyword.builder().user(User.builder().id(userId).build()).keyword(k).build());
        for (Keyword k : kt) userKwRepo.save(UserKeyword.builder().user(User.builder().id(userId).build()).keyword(k).build());
    }

    @Transactional
    public void updateProfileImage(Long userId, String imageUrl) {
        Profile p = profileRepo.findByUserId(userId).orElseThrow();
        p.setProfileImage(imageUrl); // 실제 파일 업로드는 Controller에서 처리
    }
}
