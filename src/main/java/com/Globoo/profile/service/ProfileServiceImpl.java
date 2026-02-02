package com.Globoo.profile.service;

import com.Globoo.profile.dto.KeywordDto;
import com.Globoo.profile.dto.LanguageDto;
import com.Globoo.profile.dto.ProfileCardRes;
import com.Globoo.profile.dto.ProfileDetailRes;
import com.Globoo.profile.store.ProfileRepository;
import com.Globoo.profile.store.ProfileSpecs;
import com.Globoo.user.domain.*;
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

        // KeywordDto 생성 시 category 인자 추가 및 String 변환 처리
        var keywordDtos = u.getUserKeywords().stream()
                .map(UserKeyword::getKeyword)
                .map(k -> new KeywordDto(
                        k.getId(),
                        k.getCategory() != null ? k.getCategory().toString() : null,
                        k.getName()
                ))
                .toList();

        String email = u.getEmail();
        if (!Objects.equals(viewerId, u.getId())) {
            email = mask(email);
        }

        // profileImage 앞의 "/" 제거 처리
        String profileImage = p.getProfileImage();
        if (profileImage != null && profileImage.startsWith("/")) {
            profileImage = profileImage.substring(1);
        }

        return new ProfileDetailRes(
                u.getId(),
                email,
                p.getNickname(),
                u.getName(),
                p.getCampus(),
                p.getCountry(),
                p.getMbti(),
                profileImage,
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
    public Page<ProfileCardRes> search(
            Campus campus,
            String nativeLang,
            String learnLang,
            List<Long> keywordIds,
            Pageable pageable
    ) {
        Specification<Profile> spec = Specification.where(ProfileSpecs.activeUser())
                .and(ProfileSpecs.eqCampus(campus))
                .and(ProfileSpecs.hasNativeLang(nativeLang))
                .and(ProfileSpecs.hasLearnLang(learnLang))
                .and(ProfileSpecs.hasAnyKeywordIds(keywordIds));

        // ✅ 핵심 수정: spec을 실제 조회에 적용한다.
        // ✅ Repository에서 EntityGraph로 연관 로딩도 같이 걸어놔서 N+1을 줄인다.
        Page<Profile> page = repo.findAll(spec, pageable);

        List<ProfileCardRes> content = page.getContent().stream()
                .map(this::convertProfileToCardDto)
                .toList();

        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Override
    public ProfileCardRes getProfileCard(Long userId) {
        Profile p = repo.findByUserIdWithUser(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "profile not found for user: " + userId
                ));

        return convertProfileToCardDto(p);
    }

    /** 공통 변환 메서드 */
    private ProfileCardRes convertProfileToCardDto(Profile p) {
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
                .map(k -> new KeywordDto(
                        k.getId(),
                        k.getCategory() != null ? k.getCategory().toString() : null,
                        k.getName()
                ))
                .toList();

        String profileImage = p.getProfileImage();
        if (profileImage != null && profileImage.startsWith("/")) {
            profileImage = profileImage.substring(1);
        }

        return new ProfileCardRes(
                u.getId(),
                p.getNickname(),
                p.getCampus(),
                p.getCountry(),
                p.getMbti(),
                profileImage,
                nativeDtos,
                learnDtos,
                keywordDtos,
                p.getInfoTitle(),
                p.getInfoContent()
        );
    }

    private String mask(String email) {
        int idx = email.indexOf('@');
        if (idx <= 1) return "****" + email.substring(idx);
        return email.charAt(0) + "****" + email.substring(idx);
    }
}
