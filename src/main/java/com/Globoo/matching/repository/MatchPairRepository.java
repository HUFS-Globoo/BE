package com.Globoo.matching.repository;

import com.Globoo.matching.domain.MatchPair;
import com.Globoo.matching.domain.MatchStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchPairRepository extends JpaRepository<MatchPair, UUID> {

    @Query("SELECT m FROM MatchPair m WHERE (m.userAId = :userId OR m.userBId = :userId) " +
            "AND (m.status = 'FOUND' OR m.status = 'ACCEPTED_ONE' OR m.status = 'ACCEPTED_BOTH')")
    Optional<MatchPair> findActiveMatchByUserId(Long userId);
}
