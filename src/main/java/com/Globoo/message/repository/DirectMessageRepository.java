package com.Globoo.message.repository;

import com.Globoo.message.domain.DirectMessage;
import com.Globoo.message.domain.DmThread;
import com.Globoo.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {
    List<DirectMessage> findByThreadOrderByCreatedAtAsc(DmThread thread);
    List<DirectMessage> findByThreadAndReceiverAndIsReadFalse(DmThread thread, User receiver);
}
