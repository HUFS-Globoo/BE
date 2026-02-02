package com.Globoo.profile.store;

import com.Globoo.user.domain.Campus;
import com.Globoo.user.domain.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProfileRepository
        extends JpaRepository<Profile, Long>, JpaSpecificationExecutor<Profile> {

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
     * 목록 조회용 (필터 + 페이징)
     * Specification으로 필터를 적용하면서, EntityGraph로 연관 로딩(N+1)을 줄인다.
     *
     * - user
     * - userLanguages.language
     * - userKeywords.keyword
     */
    @Override
    @EntityGraph(attributePaths = {
            "user",
            "user.userLanguages",
            "user.userLanguages.language",
            "user.userKeywords",
            "user.userKeywords.keyword"
    })
    Page<Profile> findAll(Specification<Profile> spec, Pageable pageable);

    /**
     * (레거시) campus만 필터가 걸린 fetch join 페이징 쿼리.
     * 현재 search에서는 사용하지 않는 것을 권장.
     * 혹시 다른 곳에서 쓰고 있으면 남겨둔다.
     */
    @Query(value = """
        select distinct p from Profile p
        join fetch p.user u
        left join fetch u.userLanguages ul
        left join fetch ul.language
        left join fetch u.userKeywords uk
        left join fetch uk.keyword
        where (:campus is null or p.campus = :campus)
        """,
            countQuery = """
        select count(distinct p) from Profile p
        where (:campus is null or p.campus = :campus)
        """)
    Page<Profile> findAllWithRelations(@Param("campus") Campus campus, Pageable pageable);

    // 닉네임 중복 체크 (회원가입 예외처리용)
    boolean existsByNickname(String nickname);
}
