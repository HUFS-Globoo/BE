package com.Globoo.message.repository;

import com.Globoo.message.domain.DmThread;
import com.Globoo.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DmThreadRepository extends JpaRepository<DmThread, Long> {
    List<DmThread> findByUser1OrUser2(User user1, User user2);
    Optional<DmThread> findByUser1AndUser2(User user1, User user2);
    Optional<DmThread> findByUser1IdAndUser2Id(Long user1Id, Long user2Id);
}
