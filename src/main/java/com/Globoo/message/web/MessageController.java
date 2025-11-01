package com.Globoo.message.web;

import com.Globoo.message.domain.DirectMessage;
import com.Globoo.message.domain.DmThread;
import com.Globoo.message.dto.MessageRequestDto;
import com.Globoo.message.service.MessageService;
import com.Globoo.user.domain.User;
import com.Globoo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UserRepository userRepository;

    // 쪽지방 목록 조회
    @GetMapping
    public List<DmThread> getThreads(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("로그인 유저 없음"));
        return messageService.getThreads(me);
    }

    // 특정 상대방과의 대화 조회
    @GetMapping("/{partnerId}")
    public List<DirectMessage> getMessages(Authentication authentication,
                                           @PathVariable Long partnerId) {
        Long userId = Long.parseLong(authentication.getName());
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("로그인 유저 없음"));
        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("파트너 유저 없음"));
        return messageService.getMessages(me, partner);
    }

    // 쪽지 보내기
    @PostMapping
    public DirectMessage sendMessage(Authentication authentication,
                                     @RequestBody MessageRequestDto dto) {
        Long userId = Long.parseLong(authentication.getName());
        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("로그인 유저 없음"));
        User receiver = userRepository.findById(dto.getPartnerId())
                .orElseThrow(() -> new RuntimeException("파트너 유저 없음"));
        return messageService.sendMessage(sender, receiver, dto.getContent());
    }

    // 쪽지 읽음 처리
    @PostMapping("/{partnerId}/read")
    public void markAsRead(Authentication authentication,
                           @PathVariable Long partnerId) {
        Long userId = Long.parseLong(authentication.getName());
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("로그인 유저 없음"));
        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("파트너 유저 없음"));
        messageService.markAsRead(me, partner);
    }
}
