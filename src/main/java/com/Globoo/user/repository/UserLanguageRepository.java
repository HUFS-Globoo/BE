// src/main/java/com/Globoo/user/repository/UserLanguageRepository.java
package com.Globoo.user.repository;

import com.Globoo.user.domain.LanguageType;
import com.Globoo.user.domain.UserLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserLanguageRepository extends JpaRepository<UserLanguage, Long> {

    // 조회
    @Query("select ul from UserLanguage ul where ul.user.id = :userId")
    List<UserLanguage> findAllByUserId(@Param("userId") Long userId);

    // 전체 삭제
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UserLanguage ul where ul.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    // 타입별 삭제 (필요하면)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UserLanguage ul where ul.user.id = :userId and ul.type = :type")
    void deleteAllByUserIdAndType(@Param("userId") Long userId,
                                  @Param("type") LanguageType type);
}
