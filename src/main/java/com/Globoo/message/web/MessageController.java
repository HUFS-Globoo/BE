package com.Globoo.message.web;

import com.Globoo.message.domain.DirectMessage;
import com.Globoo.message.domain.DmThread;
import com.Globoo.message.dto.MessageReqDto;
import com.Globoo.message.service.MessageService;
import com.Globoo.user.domain.User;
import com.Globoo.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
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


    @GetMapping
    @Operation(summary = "쪽지방 목록 조회", description = "로그인 한 유저의 쪽지방 목록 조회")
    public List<DmThread> getThreads(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("로그인 유저 없음"));
        return messageService.getThreads(me);
    }


    @GetMapping("/{partnerId}")
    @Operation(summary = "쪽지 목록 조회", description = "특정 상대방과 나눈 1:1 쪽지 목록 조회")
    public List<DirectMessage> getMessages(Authentication authentication,
                                           @PathVariable Long partnerId) {
        Long userId = Long.parseLong(authentication.getName());
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("로그인 유저 없음"));
        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("파트너 유저 없음"));
        return messageService.getMessages(me, partner);
    }


    @PostMapping
    @Operation(summary = "쪽지 보내기/쪽지방 생성", description = "쪽지 보내기, 쪽지방이 없으면 쪽지방 생성")
    public DirectMessage sendMessage(Authentication authentication,
                                     @RequestBody MessageReqDto dto) {
        Long userId = Long.parseLong(authentication.getName());
        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("로그인 유저 없음"));
        User receiver = userRepository.findById(dto.getPartnerId())
                .orElseThrow(() -> new RuntimeException("파트너 유저 없음"));
        return messageService.sendMessage(sender, receiver, dto.getContent());
    }
//쪽지방이 잇는경우에는 처리되엇는지에 대해

    @PostMapping("/{partnerId}/read")
    @Operation(summary = "쪽지 읽음 처리", description = "상대방이 쪽지를 읽으면 쪽지 읽음 처리")
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
