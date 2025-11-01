// src/main/java/com/Globoo/user/repository/UserKeywordRepository.java
package com.Globoo.user.repository;

import com.Globoo.user.domain.UserKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserKeywordRepository extends JpaRepository<UserKeyword, Long> {
    List<UserKeyword> findAllByUserId(Long userId);
    void deleteByUserId(Long userId);
}
