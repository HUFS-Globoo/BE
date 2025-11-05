package com.Globoo.message.service;

import com.Globoo.message.domain.DirectMessage;
import com.Globoo.message.domain.DmThread;
import com.Globoo.message.repository.DirectMessageRepository;
import com.Globoo.message.repository.DmThreadRepository;
import com.Globoo.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    private final DmThreadRepository threadRepository;
    private final DirectMessageRepository messageRepository;


    // 조회 전용 메서드 (readOnly = true)
    @Transactional(readOnly = true)
    public List<DmThread> getThreads(User user) {
        return threadRepository.findByUser1OrUser2(user, user);
    }

    @Transactional(readOnly = true)
    public List<DirectMessage> getMessages(User me, User partner) {
        Optional<DmThread> threadOpt = findThread(me, partner);
        if (threadOpt.isEmpty()) return List.of(); // Thread 없으면 빈 리스트 반환
        return messageRepository.findByThreadOrderByCreatedAtAsc(threadOpt.get());
    }


    // 쓰기 가능한 메서드
    public DmThread getOrCreateThread(User me, User partner) {
        return threadRepository.findByUser1AndUser2(me, partner)
                .or(() -> threadRepository.findByUser1AndUser2(partner, me))
                .orElseGet(() -> threadRepository.save(
                        DmThread.builder().user1(me).user2(partner).build()
                ));
    }

    public DirectMessage sendMessage(User sender, User receiver, String content) {
        DmThread thread = getOrCreateThread(sender, receiver);
        DirectMessage message = DirectMessage.builder()
                .thread(thread)
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .build();
        return messageRepository.save(message);
    }

    public void markAsRead(User me, User partner) {
        DmThread thread = getOrCreateThread(me, partner);
        List<DirectMessage> unread = messageRepository.findByThreadAndReceiverAndIsReadFalse(thread, me);
        unread.forEach(DirectMessage::markAsRead);
    }


    // 조회용 Thread 찾기 (readOnly 안전)
    @Transactional(readOnly = true)
    public Optional<DmThread> findThread(User me, User partner) {
        return threadRepository.findByUser1AndUser2(me, partner)
                .or(() -> threadRepository.findByUser1AndUser2(partner, me));
    }
}
