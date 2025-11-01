// src/main/java/com/Globoo/user/repository/UserLanguageRepository.java
package com.Globoo.user.repository;

import com.Globoo.user.domain.LanguageType;
import com.Globoo.user.domain.UserLanguage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserLanguageRepository extends JpaRepository<UserLanguage, Long> {
    List<UserLanguage> findAllByUserId(Long userId);
    void deleteByUserId(Long userId);
    void deleteByUserIdAndType(Long userId, LanguageType type);
}
