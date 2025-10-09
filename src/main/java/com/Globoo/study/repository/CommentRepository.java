package com.Globoo.study.repository;


import com.Globoo.study.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> { }
