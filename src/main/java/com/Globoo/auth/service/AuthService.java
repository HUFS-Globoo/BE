package com.Globoo.auth.service;

import com.Globoo.auth.domain.RefreshToken;
import com.Globoo.auth.dto.*;
import com.Globoo.auth.repository.RefreshTokenRepository;
import com.Globoo.common.security.JwtTokenProvider;
import com.Globoo.profile.store.ProfileRepository;
import com.Globoo.user.domain.*;
import com.Globoo.user.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final RefreshTokenRepository rtRepo;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwt;
    private final EmailVerificationService emailVerif;

    // 회원가입 시 함께 저장에 필요한 레포지토리
    private final ProfileRepository profileRepo;
    private final LanguageRepository languageRepo;
    private final UserLanguageRepository userLanguageRepo;
    private final KeywordRepository keywordRepo;
    private final UserKeywordRepository userKeywordRepo;

    @Transactional
    public SignupRes signup(SignupReq dto) {
        if (userRepo.existsByEmail(dto.email())) throw new IllegalArgumentException("email exists");
        if (userRepo.existsByUsername(dto.username())) throw new IllegalArgumentException("username exists");

        // 1) users
        User u = userRepo.save(User.builder()
                .email(dto.email())
                .username(dto.username())
                .password(encoder.encode(dto.password()))
                .name(dto.name())
                // .phoneNumber(dto.phoneNumber()) // User에 필드 있으면 사용
                .schoolVerified(false)
                .build());

        // 2) profiles
        profileRepo.save(Profile.builder()
                .user(u)
                .nickname(dto.nickname())
                .birthDate(dto.birthDate())
                .gender(dto.gender())
                .campus(dto.campus())
                .country(dto.nationalityCode())
                .mbti(dto.mbti())
                .build());

        // 3) user_languages (NATIVE / LEARN)
        Language nativeLang = languageRepo.findById(dto.nativeLanguageCode())
                .orElseThrow(() -> new IllegalArgumentException("Unknown language: " + dto.nativeLanguageCode()));
        userLanguageRepo.save(UserLanguage.builder()
                .user(u).language(nativeLang).type(LanguageType.NATIVE).build());

        Language learnLang = languageRepo.findById(dto.preferredLanguageCode())
                .orElseThrow(() -> new IllegalArgumentException("Unknown language: " + dto.preferredLanguageCode()));
        userLanguageRepo.save(UserLanguage.builder()
                .user(u).language(learnLang).type(LanguageType.LEARN).build());

        // 4) user_keywords (카테고리별 최대 10개, 중복 입력 정리, 자동 생성 금지)
        List<String> personality = Optional.ofNullable(dto.personalityKeywords()).orElse(List.of());
        List<String> hobby       = Optional.ofNullable(dto.hobbyKeywords()).orElse(List.of());
        List<String> topic       = Optional.ofNullable(dto.topicKeywords()).orElse(List.of());

        if (personality.size() > 10 || hobby.size() > 10 || topic.size() > 10) {
            throw new IllegalArgumentException("each keyword list must be <= 10");
        }

        var pNames = toDedupedSet(personality);
        var hNames = toDedupedSet(hobby);
        var tNames = toDedupedSet(topic);

        // 현재 스키마: name 전역 유일 → name으로 벌크 조회
        List<Keyword> pFound = pNames.isEmpty() ? List.of() : keywordRepo.findAllByNameIn(pNames);
        List<Keyword> hFound = hNames.isEmpty() ? List.of() : keywordRepo.findAllByNameIn(hNames);
        List<Keyword> tFound = tNames.isEmpty() ? List.of() : keywordRepo.findAllByNameIn(tNames);

        assertNoMissing("PERSONALITY", pNames, pFound);
        assertNoMissing("HOBBY",       hNames, hFound);
        assertNoMissing("TOPIC",       tNames, tFound);

        var toSave = new ArrayList<UserKeyword>(pFound.size() + hFound.size() + tFound.size());
        for (Keyword kw : pFound) toSave.add(UserKeyword.builder().user(u).keyword(kw).build());
        for (Keyword kw : hFound) toSave.add(UserKeyword.builder().user(u).keyword(kw).build());
        for (Keyword kw : tFound) toSave.add(UserKeyword.builder().user(u).keyword(kw).build());
        userKeywordRepo.saveAll(toSave);

        // 5) 이메일 인증 발송
        emailVerif.issueAndSend(u);

        return new SignupRes(u.getId(), u.getEmail(), u.getUsername(), u.getName(), u.isSchoolVerified());
    }

    @Transactional
    public OkRes resend(ResendReq req) {
        User u = userRepo.findByEmail(req.email()).orElseThrow();
        if (u.isSchoolVerified()) throw new IllegalStateException("already verified");
        emailVerif.assertResendAllowed(u.getId());
        emailVerif.issueAndSend(u);
        return new OkRes(true);
    }

    @Transactional
    public TokenRes login(LoginReq req) {
        User u = userRepo.findByEmail(req.email()).orElseThrow(() -> new IllegalArgumentException("invalid"));
        if (!encoder.matches(req.password(), u.getPassword()))
            throw new IllegalArgumentException("invalid");
        if (!u.isSchoolVerified()) throw new AccessDeniedException("email not verified");

        String access = jwt.createAccessToken(u.getId(), u.getEmail());
        String refresh = UUID.randomUUID().toString();
        rtRepo.save(RefreshToken.builder()
                .user(u).token(refresh).expiresAt(LocalDateTime.now().plusDays(14)).build());
        return new TokenRes(access, refresh, "Bearer", jwt.getAccessTokenValiditySec());
    }

    @Transactional
    public TokenRes refresh(String refreshToken) {
        RefreshToken rt = rtRepo.findByToken(refreshToken).orElseThrow(() -> new IllegalArgumentException("invalid"));
        if (rt.isExpired() || rt.isRevoked()) throw new IllegalStateException("expired/revoked");
        if (!rt.getUser().isSchoolVerified()) throw new AccessDeniedException("email not verified");
        String access = jwt.createAccessToken(rt.getUser().getId(), rt.getUser().getEmail());
        return new TokenRes(access, refreshToken, "Bearer", jwt.getAccessTokenValiditySec());
    }

    @Transactional
    public OkRes logout(String refreshToken) {
        rtRepo.findByToken(refreshToken).ifPresent(t -> t.setRevokedAt(LocalDateTime.now()));
        return new OkRes(true);
    }

    // ---------- utils ----------
    private static LinkedHashSet<String> toDedupedSet(List<String> src) {
        return src.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private static void assertNoMissing(String categoryLabel,
                                        Set<String> requested,
                                        List<Keyword> found) {
        var foundNames = found.stream().map(Keyword::getName).collect(java.util.stream.Collectors.toSet());
        var missing = new LinkedHashSet<>(requested);
        missing.removeAll(foundNames);
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Unknown " + categoryLabel + " keywords: " + missing);
        }
    }
}
