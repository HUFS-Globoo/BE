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
// 저번에 로그보니까 에러는 아니고 n+1로 성능 저하 문제가 생길 수 있다는 경고보고 수정해용 11/10
    /** 유저 ID로 프로필 상세 (fetch join으로 N+1 완전 차단) */
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

    /** 프로필 ID로 상세 (fetch join으로 N+1 완전 차단) */
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

    /** 기본 CRUD용 */
    Optional<Profile> findByUserId(Long userId);

    /**
     * 목록 조회용
     * N+1 문제를 해결하기 위해 user, userLanguages.language, userKeywords.keyword 전부 즉시 로드
     * Pageable 지원 그대로 유지됨
     */
    @Override
    @EntityGraph(attributePaths = {
            "user",
            "user.userLanguages.language",
            "user.userKeywords.keyword"
    })
    Page<Profile> findAll(@Nullable Specification<Profile> spec, Pageable pageable);
}
