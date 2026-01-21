package com.Globoo.study.DTO;

import com.Globoo.study.domain.StudyMember;
import com.Globoo.user.domain.Campus;

public record StudyApplicantRes(
        Long userId,
        String nickname,
        String profileImageUrl,
        String country,
        String mbti,
        Campus campus
) {
    public static StudyApplicantRes from(StudyMember sm) {
        var u = sm.getUser();
        var p = u.getProfile();
        return new StudyApplicantRes(
                u.getId(),
                p != null ? p.getNickname() : u.getName(),
                p != null ? p.getProfileImage() : null,
                p != null ? p.getCountry() : null,
                p != null ? p.getMbti() : null,
                p != null ? p.getCampus() : null
        );
    }
}
