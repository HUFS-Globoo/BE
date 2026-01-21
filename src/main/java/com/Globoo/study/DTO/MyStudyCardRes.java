package com.Globoo.study.DTO;

import com.Globoo.study.domain.StudyPost;

import java.time.LocalDateTime;
import java.util.List;

public record MyStudyCardRes(
        Long studyId,
        String title,
        String status,
        Integer capacity,
        Integer applicantCount,
        List<StudyApplicantRes> applicantPreview,
        LocalDateTime createdAt
) {
    public static MyStudyCardRes of(StudyPost sp, int count, List<StudyApplicantRes> preview) {
        return new MyStudyCardRes(
                sp.getId(),
                sp.getTitle(),
                sp.getStatus(),
                sp.getCapacity(),
                count,
                preview,
                sp.getCreatedAt()
        );
    }
}