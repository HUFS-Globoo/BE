// src/main/java/com/Globoo/matching/repository/MatchPairRepository.java
package com.Globoo.matching.repository;

import com.Globoo.matching.domain.MatchPair;
import com.Globoo.matching.domain.MatchStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchPairRepository extends JpaRepository<MatchPair, UUID> {

    /**
     * 유저가 참여 중인 active 매칭(FOUND / ACCEPTED_ONE / ACCEPTED_BOTH) 중에서
     * matchedAt 기준으로 가장 최근 것 하나만 반환한다.
     *
     * MatchingService.getActiveMatch(...) 에서 사용하는 헬퍼 메서드.
     */
    default Optional<MatchPair> findActiveMatchByUserId(Long userId) {
        List<MatchStatus> activeStatuses = List.of(
                MatchStatus.FOUND,
                MatchStatus.ACCEPTED_ONE,
                MatchStatus.ACCEPTED_BOTH
        );

        return findLatestActiveMatchByUserId(userId, activeStatuses);
    }

    /**
     * (userAId = :userId OR userBId = :userId)
     * AND status IN (:statuses)
     * 를 명시적으로 괄호로 묶어서 조회.
     *
     * - matchedAt 내림차순 정렬 후 첫 번째 한 건만 조회
     */
    @Query("""
        SELECT m
        FROM MatchPair m
        WHERE (m.userAId = :userId OR m.userBId = :userId)
          AND m.status IN :statuses
        ORDER BY m.matchedAt DESC
        """)
    Optional<MatchPair> findLatestActiveMatchByUserId(
            @Param("userId") Long userId,
            @Param("statuses") Collection<MatchStatus> statuses
    );

    /** 동시 수락 경쟁 방지용 행 잠금 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MatchPair m WHERE m.id = :id")
    Optional<MatchPair> findByIdForUpdate(@Param("id") UUID id);
}
// 이중 accepted 방지