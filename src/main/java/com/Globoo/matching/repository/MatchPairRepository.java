// src/main/java/com/Globoo/matching/repository/MatchPairRepository.java
package com.Globoo.matching.repository;

import com.Globoo.matching.domain.MatchPair;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface MatchPairRepository extends JpaRepository<MatchPair, UUID> {

    @Query("""
           SELECT m FROM MatchPair m
           WHERE (m.userAId = :userId OR m.userBId = :userId)
             AND (m.status = 'FOUND' OR m.status = 'ACCEPTED_ONE' OR m.status = 'ACCEPTED_BOTH')
           """)
    Optional<MatchPair> findActiveMatchByUserId(Long userId);

    /** 동시 수락 경쟁 방지용 행 잠금 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MatchPair m WHERE m.id = :id")
    Optional<MatchPair> findByIdForUpdate(UUID id);
}
