package com.Globoo.matching.repository;

import com.Globoo.matching.domain.MatchQueue;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional; // Optional 임포트 추가

@Repository
public interface MatchQueueRepository extends JpaRepository<MatchQueue, Long> {

    boolean existsByUserIdAndActiveTrue(Long userId);

    // leaveQueue 서비스를 위해 추가
    Optional<MatchQueue> findByUserIdAndActiveTrue(Long userId);

    @Query(value = "SELECT * FROM match_queue WHERE active = true ORDER BY enqueued_at ASC LIMIT 2", nativeQuery = true)
    List<MatchQueue> findTop2ByActiveTrueOrderByEnqueuedAtAsc();
}