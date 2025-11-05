package com.Globoo.profile.service;

import com.Globoo.profile.dto.ProfileCardRes;
import com.Globoo.profile.dto.ProfileDetailRes;
import com.Globoo.user.domain.Campus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProfileService {
    ProfileDetailRes getDetail(Long userId, Long viewerId);

    Page<ProfileCardRes> search(
            Campus campus,
            String nativeLang,
            String learnLang,
            List<Long> keywordIds,
            Pageable pageable
    );
    // getProfileCard 메서드 선언 추가.
    ProfileCardRes getProfileCard(Long userId);
}
