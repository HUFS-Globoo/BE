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

    //마이페이지 - 내가 작성한 댓글 목록 (기존: 댓글 + 내 작성자 정보까지만)
    @EntityGraph(attributePaths = {"studyPost", "user", "user.profile"})
    @Query("SELECT c FROM Comment c " +
            "WHERE c.user.id = :userId " +
            "ORDER BY c.createdAt DESC")
    List<Comment> findAllByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    //(추가) 마이페이지 - 내가 작성한 댓글 + 스터디글 요약 + (나/게시글작성자) 프로필/국적까지 한 번에
    // - sp.members fetch 때문에 중복 row가 생길 수 있어서 DISTINCT
    @Query("""
        SELECT DISTINCT c FROM Comment c
        JOIN FETCH c.studyPost sp
        LEFT JOIN FETCH sp.members m
        LEFT JOIN FETCH sp.user spu
        LEFT JOIN FETCH spu.profile spp
        JOIN FETCH c.user cu
        LEFT JOIN FETCH cu.profile cup
        WHERE cu.id = :userId
        ORDER BY c.createdAt DESC
    """)
    List<Comment> findMyCommentsForMyPage(@Param("userId") Long userId);
}
