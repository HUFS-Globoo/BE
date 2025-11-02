// src/main/java/com/Globoo/profile/service/ProfileServiceImpl.java
package com.Globoo.profile.service;

import com.Globoo.profile.dto.KeywordDto;
import com.Globoo.profile.dto.LanguageDto;
import com.Globoo.profile.dto.ProfileCardRes;
import com.Globoo.profile.dto.ProfileDetailRes;
import com.Globoo.profile.store.ProfileRepository;
import com.Globoo.profile.store.ProfileSpecs;
import com.Globoo.user.domain.Campus;
import com.Globoo.user.domain.LanguageType;
import com.Globoo.user.domain.Profile;
import com.Globoo.user.domain.User;
import com.Globoo.user.domain.UserKeyword;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository repo;

    @Override
    public ProfileDetailRes getDetail(Long id, Long viewerId) {
        // 1) 유저ID로 시도 -> 2) 실패하면 프로필ID로 시도
        Profile p = repo.findByUserIdWithUser(id)
                .orElseGet(() -> repo.findByProfileIdWithUser(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "profile not found")));

        User u = p.getUser();

        var userLangs = u.getUserLanguages();
        List<LanguageDto> nativeDtos = userLangs.stream()
                .filter(l -> l.getType() == LanguageType.NATIVE)
                .map(l -> new LanguageDto(l.getLanguage().getCode(), l.getLanguage().getName()))
                .toList();

        List<LanguageDto> learnDtos = userLangs.stream()
                .filter(l -> l.getType() == LanguageType.LEARN)
                .map(l -> new LanguageDto(l.getLanguage().getCode(), l.getLanguage().getName()))
                .toList();

        var keywordDtos = u.getUserKeywords().stream()
                .map(UserKeyword::getKeyword)
                .map(k -> new KeywordDto(k.getId(), k.getName()))
                .toList();

        String email = u.getEmail();
        if (!Objects.equals(viewerId, u.getId())) {
            email = mask(email);
        }

        return new ProfileDetailRes(
                u.getId(),
                email,
                p.getNickname(),
                u.getName(),
                p.getCampus(),
                p.getCountry(),
                p.getMbti(),
                p.getProfileImage(),
                p.getInfoTitle(),
                p.getInfoContent(),
                p.getBirthDate() != null ? p.getBirthDate().toString() : null,
                p.getGender(),
                nativeDtos,
                learnDtos,
                keywordDtos
        );
    }

    @Override
    public Page<ProfileCardRes> search(Campus campus, String nativeLang, String learnLang,
                                       List<Long> keywordIds, Pageable pageable) {
        Specification<Profile> spec = Specification.where(ProfileSpecs.activeUser())
                .and(ProfileSpecs.eqCampus(campus))
                .and(ProfileSpecs.hasNativeLang(nativeLang))
                .and(ProfileSpecs.hasLearnLang(learnLang))
                .and(ProfileSpecs.hasAnyKeywordIds(keywordIds));

        Page<Profile> page = repo.findAll(spec, pageable);

        List<ProfileCardRes> content = page.getContent().stream().map(p -> {
            User u = p.getUser();

            var userLangs = u.getUserLanguages();
            List<LanguageDto> nativeDtos = userLangs.stream()
                    .filter(l -> l.getType() == LanguageType.NATIVE)
                    .map(l -> new LanguageDto(l.getLanguage().getCode(), l.getLanguage().getName()))
                    .toList();

            List<LanguageDto> learnDtos = userLangs.stream()
                    .filter(l -> l.getType() == LanguageType.LEARN)
                    .map(l -> new LanguageDto(l.getLanguage().getCode(), l.getLanguage().getName()))
                    .toList();

            var keywordDtos = u.getUserKeywords().stream()
                    .map(UserKeyword::getKeyword)
                    .map(k -> new KeywordDto(k.getId(), k.getName()))
                    .toList();

            return new ProfileCardRes(
                    u.getId(),
                    p.getNickname(),
                    p.getCampus(),
                    p.getCountry(),
                    p.getMbti(),
                    p.getProfileImage(),
                    nativeDtos,
                    learnDtos,
                    keywordDtos,
                    p.getInfoTitle(),
                    p.getInfoContent()
            );
        }).collect(Collectors.toList());

        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    private String mask(String email) {
        int idx = email.indexOf('@');
        if (idx <= 1) return "****" + email.substring(idx);
        return email.charAt(0) + "****" + email.substring(idx);
    }
}
//여기 오류 되어있는거 걍 무시해도 코드 돌아가용