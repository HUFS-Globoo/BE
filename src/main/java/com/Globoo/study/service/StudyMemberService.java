package com.Globoo.study.service;

import com.Globoo.common.logging.LoggerService;
import com.Globoo.study.DTO.StudyApplicantRes;
import com.Globoo.study.domain.StudyMember;
import com.Globoo.study.domain.StudyPost;
import com.Globoo.study.repository.StudyMemberRepository;
import com.Globoo.study.repository.StudyPostRepository;
import com.Globoo.user.domain.User;
import com.Globoo.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StudyMemberService {

    private final StudyPostRepository studyPostRepository;
    private final UserRepository userRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final LoggerService loggerService;

    public StudyMemberService(StudyPostRepository studyPostRepository,
                              UserRepository userRepository,
                              StudyMemberRepository studyMemberRepository,
                              LoggerService loggerService) {
        this.studyPostRepository = studyPostRepository;
        this.userRepository = userRepository;
        this.studyMemberRepository = studyMemberRepository;
        this.loggerService = loggerService;
    }

    @Transactional(readOnly = true)
    public List<StudyApplicantRes> getApplicants(Long postId) {
        return studyMemberRepository.findAllByPostIdWithUserProfile(postId).stream()
                .map(com.Globoo.study.DTO.StudyApplicantRes::from)
                .toList();
    }

    public void joinStudy(Long postId, Long currentUserId) {
        StudyPost post = studyPostRepository.findByIdWithMembers(postId)
                .orElseThrow(() -> new IllegalArgumentException("스터디를 찾을 수 없습니다. id=" + postId));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        int currentParticipants = post.getMembers().size();

        if ("마감".equals(post.getStatus())) {
            throw new IllegalArgumentException("STUDY_ALREADY_CLOSED");
        }

        if (currentParticipants >= post.getCapacity()) {
            post.setStatus("마감");
            throw new IllegalArgumentException("STUDY_CAPACITY_EXCEEDED");
        }

        boolean alreadyJoined = studyMemberRepository.existsByStudyPostIdAndUserId(postId, currentUserId);
        if (alreadyJoined) {
            throw new IllegalArgumentException("STUDY_ALREADY_JOINED");
        }

        StudyMember newMember = StudyMember.builder()
                .user(user)
                .studyPost(post)
                .build();

        studyMemberRepository.save(newMember);

        loggerService.logEvent("EVENT_JOIN", currentUserId);

        if (currentParticipants + 1 >= post.getCapacity()) {
            post.setStatus("마감");
        }
    }
}