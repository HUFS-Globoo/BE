package com.Globoo.study.repository;


import com.Globoo.study.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByStudyPostId(Long studyPostId, Pageable pageable);
}
