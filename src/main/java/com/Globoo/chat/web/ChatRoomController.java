package com.Globoo.chat.web;

import com.Globoo.chat.dto.ChatMessageGetResDto;
import com.Globoo.chat.dto.ChatRoomCreateReqDto;
import com.Globoo.chat.dto.ChatRoomCreateResDto;
import com.Globoo.chat.dto.ChatRoomGetResDto;
import com.Globoo.chat.service.ChatService;
import com.Globoo.common.security.SecurityUtils; // (SecurityUtils 사용)
import com.Globoo.common.web.ApiResponse; // (공통 응답 DTO 가정)
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatService chatService;

    /** (매칭 서비스가) 1:1 채팅방 생성 */
    @PostMapping
    public ResponseEntity<ApiResponse<ChatRoomCreateResDto>> createChatRoom(
            @RequestBody ChatRoomCreateReqDto dto
    ) {
        // (주의: 이 API는 매칭된 클라이언트가 아닌, '매칭 서비스'가 호출해야 합니다.)
        // (또는, 클라이언트가 호출하더라도 인증된 사용자가 본인 ID를 넣어야 합니다.)
        Long currentUserId = SecurityUtils.getCurrentUserId();
        ChatRoomCreateResDto response = chatService.createChatRoom(dto, currentUserId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    /** (클라이언트가) 내 채팅방 목록 조회 */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatRoomGetResDto>>> getMyChatRooms() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        List<ChatRoomGetResDto> response = chatService.findMyChatRooms(currentUserId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    /** (클라이언트가) 특정 채팅방의 이전 메시지 조회 */
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageGetResDto>>> getChatRoomMessages(
            @PathVariable Long roomId
    ) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        List<ChatMessageGetResDto> response = chatService.findChatRoomMessages(roomId, currentUserId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
}