package com.Globoo.study.service;

import com.Globoo.study.DTO.MyStudyCardRes;
import com.Globoo.study.DTO.StudyApplicantRes;
import com.Globoo.study.domain.StudyPost;
import com.Globoo.study.repository.StudyPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MyStudyService {

    private final StudyPostRepository studyPostRepository;

    public MyStudyService(StudyPostRepository studyPostRepository) {
        this.studyPostRepository = studyPostRepository;
    }

    // 내가 신청한 스터디 목록
    public List<MyStudyCardRes> getApplied(Long userId) {
        List<StudyPost> posts = studyPostRepository.findAppliedByUserIdWithUserProfileMembers(userId);

        return posts.stream()
                .map(sp -> {
                    int count = sp.getMembers() != null ? sp.getMembers().size() : 0;
                    List<StudyApplicantRes> preview = sp.getMembers().stream()
                            .limit(5)
                            .map(StudyApplicantRes::from)
                            .toList();
                    return MyStudyCardRes.of(sp, count, preview);
                })
                .toList();
    }

    // 내가 올린 스터디 목록
    public List<MyStudyCardRes> getOwned(Long userId) {
        List<StudyPost> posts = studyPostRepository.findOwnedByUserIdWithMembers(userId);

        return posts.stream()
                .map(sp -> {
                    int count = sp.getMembers() != null ? sp.getMembers().size() : 0;
                    List<StudyApplicantRes> preview = sp.getMembers().stream()
                            .limit(5)
                            .map(StudyApplicantRes::from)
                            .toList();
                    return MyStudyCardRes.of(sp, count, preview);
                })
                .toList();
    }
}
