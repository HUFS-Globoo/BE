package com.Globoo.study.repository;

import com.Globoo.study.domain.StudyPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudyPostRepository extends JpaRepository<StudyPost, Long>, JpaSpecificationExecutor<StudyPost> {

    /**
     * ID로 게시글 조회 시, N+1 방지를 위해 user, profile, members를 fetch join
     * (기존 findByIdWithUserAndProfile에서 members 추가)
     */
    @Query("SELECT sp FROM StudyPost sp " +
            "LEFT JOIN FETCH sp.user u " +
            "LEFT JOIN FETCH u.profile p " +
            "LEFT JOIN FETCH sp.members m " + // ✅ (추가)
            "WHERE sp.id = :id")
    Optional<StudyPost> findByIdWithUserAndProfileAndMembers(@Param("id") Long id);

    /**
     * ✅ (추가) 스터디 가입/탈퇴 시 사용할 members fetch join 쿼리
     */
    @Query("SELECT sp FROM StudyPost sp " +
            "LEFT JOIN FETCH sp.members m " +
            "WHERE sp.id = :id")
    Optional<StudyPost> findByIdWithMembers(@Param("id") Long id);
}