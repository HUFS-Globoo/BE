package com.Globoo.matching.repository;

import com.Globoo.matching.domain.MatchQueue;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchQueueRepository extends JpaRepository<MatchQueue, Long> {

    boolean existsByUserIdAndActiveTrue(Long userId);

    Optional<MatchQueue> findByUserIdAndActiveTrue(Long userId);

    @Query("SELECT mq FROM MatchQueue mq WHERE mq.active = true AND mq.userId != :userId ORDER BY mq.enqueuedAt ASC")
    List<MatchQueue> findAllByActiveTrueAndUserIdNot(@Param("userId") Long userId);

    Optional<MatchQueue> findFirstByUserIdOrderByEnqueuedAtDesc(Long userId);
    List<MatchQueue> findAllByActiveTrueAndEnqueuedAtBefore(LocalDateTime threshold);
}