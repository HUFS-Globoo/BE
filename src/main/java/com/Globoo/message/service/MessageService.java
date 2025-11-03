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

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    private final DmThreadRepository threadRepository;
    private final DirectMessageRepository messageRepository;

    @Transactional(readOnly = true)
    public List<DmThread> getThreads(User user) {
        return threadRepository.findByUser1OrUser2(user, user);
    }

    public DmThread getOrCreateThread(User me, User partner) {
        return threadRepository.findByUser1AndUser2(me, partner)
                .or(() -> threadRepository.findByUser1AndUser2(partner, me))
                .orElseGet(() -> threadRepository.save(DmThread.builder()
                        .user1(me)
                        .user2(partner)
                        .build()));
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

    @Transactional(readOnly = true)
    public List<DirectMessage> getMessages(User me, User partner) {
        DmThread thread = getOrCreateThread(me, partner);
        return messageRepository.findByThreadOrderByCreatedAtAsc(thread);
    }

    public void markAsRead(User me, User partner) {
        DmThread thread = getOrCreateThread(me, partner);
        List<DirectMessage> unread = messageRepository.findByThreadAndReceiverAndIsReadFalse(thread, me);
        unread.forEach(DirectMessage::markAsRead);
    }
}
