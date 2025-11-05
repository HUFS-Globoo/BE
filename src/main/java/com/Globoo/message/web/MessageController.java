package com.Globoo.message.web;

import com.Globoo.message.dto.MessageReqDto;
import com.Globoo.message.dto.MessageResDto;
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

    // 쪽지방 목록 조회
    @GetMapping
    @Operation(summary = "쪽지방 목록 조회", description = "로그인 한 유저의 쪽지방 목록 조회")
    public List<MessageResDto.ThreadDto> getThreads(Authentication authentication) {
        User me = getLoggedInUser(authentication);
        return messageService.getThreads(me); // DTO 반환
    }

    // 특정 상대와 쪽지 목록 조회
    @GetMapping("/{partnerId}")
    @Operation(summary = "쪽지 목록 조회", description = "특정 상대방과 나눈 1:1 쪽지 목록 조회")
    public List<MessageResDto> getMessages(Authentication authentication,
                                           @PathVariable Long partnerId) {
        User me = getLoggedInUser(authentication);
        User partner = getUserById(partnerId);
        return messageService.getMessages(me, partner); // DTO 반환
    }

    // 쪽지 보내기
    @PostMapping
    @Operation(summary = "쪽지 보내기/쪽지방 생성", description = "쪽지 보내기, 쪽지방이 없으면 쪽지방 생성")
    public MessageResDto sendMessage(Authentication authentication,
                                     @RequestBody MessageReqDto dto) {
        User sender = getLoggedInUser(authentication);
        User receiver = getUserById(dto.getPartnerId());
        return messageService.sendMessage(sender, receiver, dto.getContent()); // DTO 반환
    }

    // 쪽지 읽음 처리
    @PostMapping("/{partnerId}/read")
    @Operation(summary = "쪽지 읽음 처리", description = "상대방이 쪽지를 읽으면 쪽지 읽음 처리")
    public void markAsRead(Authentication authentication,
                           @PathVariable Long partnerId) {
        User me = getLoggedInUser(authentication);
        User partner = getUserById(partnerId);
        messageService.markAsRead(me, partner);
    }

    // 유틸 메서드
    private User getLoggedInUser(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("로그인 유저 없음"));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("파트너 유저 없음"));
    }
}
