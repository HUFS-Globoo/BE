// src/main/java/com/Globoo/profile/store/ProfileRepository.java
package com.Globoo.profile.store;

import com.Globoo.user.domain.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface ProfileRepository
        extends JpaRepository<Profile, Long>, JpaSpecificationExecutor<Profile> {

    // 유저ID로 상세(페치조인)
    @Query("""
        select distinct p from Profile p
        join fetch p.user u
        left join fetch u.userLanguages ul
        left join fetch ul.language
        left join fetch u.userKeywords uk
        left join fetch uk.keyword
        where u.id = :uid
    """)
    Optional<Profile> findByUserIdWithUser(@Param("uid") Long userId);

    // ★ 프로필ID로 상세(페치조인) - FE가 profileId를 넘겨도 대응
    @Query("""
        select distinct p from Profile p
        join fetch p.user u
        left join fetch u.userLanguages ul
        left join fetch ul.language
        left join fetch u.userKeywords uk
        left join fetch uk.keyword
        where p.id = :pid
    """)
    Optional<Profile> findByProfileIdWithUser(@Param("pid") Long profileId);

    // 단순 CRUD용
    Optional<Profile> findByUserId(Long userId);

    // 목록 조회는 N+1 줄이기 위해 user만 즉시 로드
    @Override
    @EntityGraph(attributePaths = {"user"})
    Page<Profile> findAll(@Nullable Specification<Profile> spec, Pageable pageable);
}
