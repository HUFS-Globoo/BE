package com.Globoo.study.repository;

import com.Globoo.study.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByStudyPostId(Long studyPostId, Pageable pageable);

    // ✅ 마이페이지 - 내가 작성한 댓글 목록
    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.studyPost sp " +
            "WHERE c.user.id = :userId " +
            "ORDER BY c.createdAt DESC")
    List<Comment> findAllByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
}
