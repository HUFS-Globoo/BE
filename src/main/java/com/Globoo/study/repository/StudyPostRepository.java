package com.Globoo.study.repository;

import com.Globoo.study.domain.StudyPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudyPostRepository extends JpaRepository<StudyPost, Long>, JpaSpecificationExecutor<StudyPost> {

    /**
     * ID로 게시글 조회 시, N+1 방지를 위해 user와 profile을 fetch join
     */
    @Query("SELECT sp FROM StudyPost sp " +
            "LEFT JOIN FETCH sp.user u " +
            "LEFT JOIN FETCH u.profile p " +
            "WHERE sp.id = :id")
    Optional<StudyPost> findByIdWithUserAndProfile(@Param("id") Long id);
}