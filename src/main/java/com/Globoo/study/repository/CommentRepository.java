package com.Globoo.study.repository;

import com.Globoo.study.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 댓글 목록 조회: user + user.profile 같이 로딩 (작성자 국적/이미지 등)
    @EntityGraph(attributePaths = {"user", "user.profile"})
    Page<Comment> findByStudyPostId(Long studyPostId, Pageable pageable);

    // 마이페이지 - 내가 작성한 댓글 목록 (작성자 정보도 필요할 수 있으니 같이 로딩)
    @EntityGraph(attributePaths = {"studyPost", "user", "user.profile"})
    @Query("SELECT c FROM Comment c " +
            "WHERE c.user.id = :userId " +
            "ORDER BY c.createdAt DESC")
    List<Comment> findAllByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
}
