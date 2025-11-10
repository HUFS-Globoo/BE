package com.Globoo.profile.store;

import com.Globoo.user.domain.Campus;
import com.Globoo.user.domain.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * 목록 조회용 (페이징 + N+1 완전 차단)
     * user, userLanguages.language, userKeywords.keyword 모두 즉시 로딩
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
}
