// src/main/java/com/Globoo/matching/repository/MatchPairRepository.java
package com.Globoo.matching.repository;

import com.Globoo.matching.domain.MatchPair;
import com.Globoo.matching.domain.MatchStatus;
import org.springframework.data.jpa.repository.*;
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

        return findFirstByUserAIdOrUserBIdAndStatusInOrderByMatchedAtDesc(
                userId,
                userId,
                activeStatuses
        );
    }

    /**
     * Spring Data JPA 가 쿼리를 자동 생성해주는 메서드.
     * - userAId = ? 또는 userBId = ?
     * - status IN ( ... )
     * - matchedAt 내림차순 정렬 후 첫 번째 한 건만 조회
     *
     * → 내부적으로 setMaxResults(1)를 사용해서 NonUniqueResultException 이 발생하지 않는다.
     */
    Optional<MatchPair> findFirstByUserAIdOrUserBIdAndStatusInOrderByMatchedAtDesc(
            Long userAId,
            Long userBId,
            Collection<MatchStatus> statuses
    );

    /** 동시 수락 경쟁 방지용 행 잠금 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MatchPair m WHERE m.id = :id")
    Optional<MatchPair> findByIdForUpdate(UUID id);
}
// 혹시나 해서 기본의 코드에서 중복되는 경우를 제외해주는 코드를 넣엇습니당