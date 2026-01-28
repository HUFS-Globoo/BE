package com.Globoo.matching.repository;

import com.Globoo.matching.domain.MatchPair;
import com.Globoo.matching.domain.MatchStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public interface MatchPairRepository extends JpaRepository<MatchPair, UUID> {

    default Optional<MatchPair> findActiveMatchByUserId(Long userId) {
        List<MatchStatus> activeStatuses = List.of(
                MatchStatus.FOUND,
                MatchStatus.ACCEPTED_ONE,
                MatchStatus.ACCEPTED_BOTH
        );
        return findLatestActiveMatchByUserId(userId, activeStatuses);
    }

    @Query("""
        SELECT m FROM MatchPair m
        WHERE (m.userAId = :userId OR m.userBId = :userId)
          AND m.status IN :statuses
        ORDER BY m.matchedAt DESC
        """)
    Optional<MatchPair> findLatestActiveMatchByUserId(
            @Param("userId") Long userId,
            @Param("statuses") Collection<MatchStatus> statuses
    );

    /**  최근 스킵한 유저 ID 목록 조회 (블랙리스트) */
    @Query("""
        SELECT CASE WHEN m.userAId = :userId THEN m.userBId ELSE m.userAId END
        FROM MatchPair m
        WHERE (m.userAId = :userId OR m.userBId = :userId)
          AND m.status = com.Globoo.matching.domain.MatchStatus.SKIPPED
          AND m.matchedAt > :threshold
        """)
    List<Long> findRecentlySkippedUserIds(@Param("userId") Long userId, @Param("threshold") LocalDateTime threshold);

    /**  응답 없는(FOUND, ACCEPTED_ONE) 오래된 매칭 조회 */
    @Query("""
        SELECT m FROM MatchPair m 
        WHERE m.status IN :statuses AND m.matchedAt < :threshold
        """)
    List<MatchPair> findStaleMatches(@Param("statuses") List<MatchStatus> statuses, @Param("threshold") LocalDateTime threshold);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MatchPair m WHERE m.id = :id")
    Optional<MatchPair> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
    SELECT m FROM MatchPair m
    WHERE m.chatRoomId = :roomId
    ORDER BY m.matchedAt DESC
    """)
    Optional<MatchPair> findLatestByChatRoomId(@Param("roomId") Long roomId);


    List<MatchPair> findByStatusAndMatchedAtBefore(MatchStatus status, LocalDateTime threshold);
}