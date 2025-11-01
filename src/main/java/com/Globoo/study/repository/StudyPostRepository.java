package com.Globoo.study.repository;

import com.Globoo.study.domain.StudyPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StudyPostRepository extends JpaRepository<StudyPost, Long>, JpaSpecificationExecutor<StudyPost> { }
