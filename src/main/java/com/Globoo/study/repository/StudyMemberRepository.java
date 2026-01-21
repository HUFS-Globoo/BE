package com.Globoo.study.repository;

import com.Globoo.study.domain.StudyMember;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

    boolean existsByStudyPostIdAndUserId(Long studyPostId, Long userId);

    /** 특정 스터디의 신청자(멤버) 전체 */
    @Query("""
        select distinct sm
        from StudyMember sm
        join fetch sm.user u
        left join fetch u.profile p
        where sm.studyPost.id = :postId
        order by sm.id desc
    """)
    List<StudyMember> findAllByPostIdWithUserProfile(@Param("postId") Long postId);

    /** 특정 스터디의 신청자 수 */
    long countByStudyPostId(Long postId);

    /** 특정 스터디의 신청자 preview (최대 N명) - Pageable로 size=5 주면 됨 */
    @Query("""
        select distinct sm
        from StudyMember sm
        join fetch sm.user u
        left join fetch u.profile p
        where sm.studyPost.id = :postId
        order by sm.id desc
    """)
    List<StudyMember> findPreviewByPostIdWithUserProfile(@Param("postId") Long postId, Pageable pageable);

    /** 내가 신청한(가입한) 스터디 postId 목록 */
    @Query("""
        select sm.studyPost.id
        from StudyMember sm
        where sm.user.id = :userId
        order by sm.id desc
    """)
    List<Long> findAppliedPostIds(@Param("userId") Long userId);
}
