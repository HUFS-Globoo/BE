package com.Globoo.study.service;

import com.Globoo.study.domain.StudyMember;
import com.Globoo.study.domain.StudyPost;
import com.Globoo.study.repository.StudyMemberRepository;
import com.Globoo.study.repository.StudyPostRepository;
import com.Globoo.user.domain.User;
import com.Globoo.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StudyMemberService {

    private final StudyPostRepository studyPostRepository;
    private final UserRepository userRepository;
    private final StudyMemberRepository studyMemberRepository;

    public StudyMemberService(StudyPostRepository studyPostRepository,
                              UserRepository userRepository,
                              StudyMemberRepository studyMemberRepository) {
        this.studyPostRepository = studyPostRepository;
        this.userRepository = userRepository;
        this.studyMemberRepository = studyMemberRepository;
    }

    /**
     * 스터디 가입 (POST /api/studies/{postId}/join)
     */
    public void joinStudy(Long postId, Long currentUserId) {

        // 1. 스터디 조회 (N+1 방지를 위해 members를 fetch join)
        StudyPost post = studyPostRepository.findByIdWithMembers(postId)
                .orElseThrow(() -> new IllegalArgumentException("스터디를 찾을 수 없습니다. id=" + postId));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        // 2. 현재 인원수
        int currentParticipants = post.getMembers().size();

        // 3. 자동 마감 로직 (체크 1: 이미 마감되었는지?)
        if ("마감".equals(post.getStatus())) {
            throw new IllegalArgumentException("STUDY_ALREADY_CLOSED");
        }

        // 4. 최대 인원수 체크 (최대 6명 제한)
        if (currentParticipants >= post.getCapacity()) {
            post.setStatus("마감");
            throw new IllegalArgumentException("STUDY_CAPACITY_EXCEEDED");
        }

        // 5. 중복 가입 체크 (500 에러 해결)
        boolean alreadyJoined = studyMemberRepository.existsByStudyPostIdAndUserId(postId, currentUserId);
        if (alreadyJoined) {
            throw new IllegalArgumentException("STUDY_ALREADY_JOINED");
        }

        // 6. 가입 처리 (StudyMember 생성)
        StudyMember newMember = StudyMember.builder()
                .user(user)
                .studyPost(post)
                // .role(StudyMember.Role.MEMBER)  Role 제거
                .build();
        studyMemberRepository.save(newMember);

        // 7. 자동 마감 로직 (체크 2: 방금 가입한 인원으로 꽉 찼는지?)
        if (currentParticipants + 1 >= post.getCapacity()) {
            post.setStatus("마감");
        }
    }
}