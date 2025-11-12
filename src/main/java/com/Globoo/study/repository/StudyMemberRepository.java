package com.Globoo.study.repository;

import com.Globoo.study.domain.StudyMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

    /**
     *  (추가) 중복 가입 방지를 위해 특정 스터디(Id)에 특정 유저(Id)가 이미 존재하는지 확인
     * StudyMemberService의 500 에러(LazyInitializationException)를 해결합니다.
     */
    boolean existsByStudyPostIdAndUserId(Long studyPostId, Long userId);
}