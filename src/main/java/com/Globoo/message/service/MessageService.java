package com.Globoo.message.service;

import com.Globoo.message.domain.DirectMessage;
import com.Globoo.message.domain.DmThread;
import com.Globoo.message.dto.MessageResDto;
import com.Globoo.message.repository.DirectMessageRepository;
import com.Globoo.message.repository.DmThreadRepository;
import com.Globoo.user.domain.Profile;
import com.Globoo.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    private final DmThreadRepository threadRepository;
    private final DirectMessageRepository messageRepository;

    // dto 변환 유틸
    private MessageResDto.UserSummaryDto toUserSummary(User user) {
        String nickname = user.getProfile() != null ? user.getProfile().getNickname() : user.getUsername();
        String nationality = user.getProfile() != null ? user.getProfile().getCountry() : "Unknown";
        return MessageResDto.UserSummaryDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(nickname)
                .nationality(nationality)
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }

    private MessageResDto toMessageResDto(DirectMessage message) {
        return MessageResDto.builder()
                .id(message.getId())
                .sender(toUserSummary(message.getSender()))
                .receiver(toUserSummary(message.getReceiver()))
                .content(message.getContent())
                .isRead(message.getIsRead())
                .createdAt(message.getCreatedAt())
                .build();
    }

    private MessageResDto.ThreadDto toThreadDto(DmThread thread) {
        List<MessageResDto> messages = thread.getMessages().stream()
                .map(this::toMessageResDto)
                .collect(Collectors.toList());
        return MessageResDto.ThreadDto.builder()
                .id(thread.getId())
                .user1(toUserSummary(thread.getUser1()))
                .user2(toUserSummary(thread.getUser2()))
                .createdAt(thread.getCreatedAt())
                .messages(messages)
                .build();
    }

    // 조회
    @Transactional(readOnly = true)
    public List<MessageResDto.ThreadDto> getThreads(User user) {
        List<DmThread> threads = threadRepository.findByUser1OrUser2(user, user);
        return threads.stream()
                .map(this::toThreadDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessageResDto> getMessages(User me, User partner) {
        Optional<DmThread> threadOpt = findThread(me, partner);
        if (threadOpt.isEmpty()) return List.of();
        return threadOpt.get().getMessages().stream()
                .map(this::toMessageResDto)
                .collect(Collectors.toList());
    }

    // 쓰기
    public DmThread getOrCreateThread(User me, User partner) {
        return threadRepository.findByUser1AndUser2(me, partner)
                .or(() -> threadRepository.findByUser1AndUser2(partner, me))
                .orElseGet(() -> threadRepository.save(
                        DmThread.builder()
                                .user1(me)
                                .user2(partner)
                                .build()
                ));
    }

    public MessageResDto sendMessage(User sender, User receiver, String content) {
        DmThread thread = getOrCreateThread(sender, receiver);
        DirectMessage message = DirectMessage.builder()
                .thread(thread)
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .build();
        DirectMessage saved = messageRepository.save(message);
        return toMessageResDto(saved);
    }

    public void markAsRead(User me, User partner) {
        DmThread thread = getOrCreateThread(me, partner);
        List<DirectMessage> unread = messageRepository.findByThreadAndReceiverAndIsReadFalse(thread, me);
        unread.forEach(DirectMessage::markAsRead);
    }

    // 조회용 Thread
    @Transactional(readOnly = true)
    public Optional<DmThread> findThread(User me, User partner) {
        return threadRepository.findByUser1AndUser2(me, partner)
                .or(() -> threadRepository.findByUser1AndUser2(partner, me));
    }
}
