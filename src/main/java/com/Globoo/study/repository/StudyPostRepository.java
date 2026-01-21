package com.Globoo.study.repository;

import com.Globoo.study.domain.StudyPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudyPostRepository extends JpaRepository<StudyPost, Long>, JpaSpecificationExecutor<StudyPost> {

    @Query("""
        SELECT sp FROM StudyPost sp
        LEFT JOIN FETCH sp.user u
        LEFT JOIN FETCH u.profile p
        LEFT JOIN FETCH sp.members m
        WHERE sp.id = :id
    """)
    Optional<StudyPost> findByIdWithUserAndProfileAndMembers(@Param("id") Long id);

    @Query("""
        SELECT sp FROM StudyPost sp
        LEFT JOIN FETCH sp.members m
        WHERE sp.id = :id
    """)
    Optional<StudyPost> findByIdWithMembers(@Param("id") Long id);

    // 마이페이지 - 내가 작성한 스터디 글 목록 (간단 버전)
    List<StudyPost> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    // 마이페이지 - 내가 작성한 스터디 글 목록 (members까지 fetch 해서 applicantCount/preview 뽑기 용)
    @Query("""
        select distinct sp
        from StudyPost sp
        left join fetch sp.members m
        where sp.user.id = :userId
        order by sp.createdAt desc
    """)
    List<StudyPost> findOwnedByUserIdWithMembers(@Param("userId") Long userId);

    // 마이페이지 - 내가 신청한 스터디 글 목록 (StudyMember 기준으로 내가 가입한 글들)
    @Query("""
        select distinct sp
        from StudyMember sm
        join sm.studyPost sp
        left join fetch sp.user u
        left join fetch u.profile p
        left join fetch sp.members m
        where sm.user.id = :userId
        order by sp.createdAt desc
    """)
    List<StudyPost> findAppliedByUserIdWithUserProfileMembers(@Param("userId") Long userId);
}
