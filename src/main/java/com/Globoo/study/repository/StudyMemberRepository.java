package com.Globoo.study.repository;

import com.Globoo.study.domain.StudyMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

}