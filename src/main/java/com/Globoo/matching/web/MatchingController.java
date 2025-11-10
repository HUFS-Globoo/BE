package com.Globoo.matching.web;

import com.Globoo.matching.domain.MatchPair;
import com.Globoo.matching.service.MatchingService;
import com.Globoo.user.domain.User; // [!!!] 1. User 객체 import
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // [!!!] 2. AuthenticationPrincipal import
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService service;

    /** 대기열 진입 */
    @PostMapping("/queue")
    // @RequestBody QueueEnterReq req -> @AuthenticationPrincipal User user
    public ResponseEntity<?> enterQueue(@AuthenticationPrincipal User user) {
        Long myUserId = user.getId(); // 토큰에서 "내" ID를 안전하게 가져옴.
        Map<String, Object> result = service.enterQueue(myUserId);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("data", result); // 서비스의 반환값을 그대로 전달
        return ResponseEntity.ok(resp);
    }

    /** 대기열 취소 */
    @DeleteMapping("/queue")
    // @RequestBody UserReq req -> @AuthenticationPrincipal User user
    public ResponseEntity<?> leaveQueue(@AuthenticationPrincipal User user) {
        Long myUserId = user.getId(); // 토큰에서 ID 추출
        service.leaveQueue(myUserId);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "Dequeued");
        return ResponseEntity.ok(resp);
    }

    /** 매칭 수락 */
    @PostMapping("/{matchId}/accept")
    // [!!!] @RequestBody UserReq req -> @AuthenticationPrincipal User user
    public ResponseEntity<?> accept(@PathVariable UUID matchId, @AuthenticationPrincipal User user) {
        Long myUserId = user.getId(); // 토큰에서 ID 추출
        Map<String, Object> data = service.accept(matchId, myUserId);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("data", data);
        return ResponseEntity.ok(resp);
    }

    /** 다음 상대 찾기(스킵) */
    @PostMapping("/{matchId}/next")
    //  @RequestBody UserReq req -> @AuthenticationPrincipal User user
    public ResponseEntity<?> next(@PathVariable UUID matchId, @AuthenticationPrincipal User user) {
        Long myUserId = user.getId(); // 토큰에서 ID 추출
        Map<String, Object> data = service.skipAndRequeue(matchId, myUserId);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("data", data);
        return ResponseEntity.ok(resp);
    }

    /** 현재 진행중 매칭 조회 (헬퍼) */
    @GetMapping("/active") //  URL에서 {userId} 삭제
    //  @PathVariable Long userId -> @AuthenticationPrincipal User user
    public ResponseEntity<?> active(@AuthenticationPrincipal User user) {
        Long myUserId = user.getId(); // 토큰에서 ID 추출
        Optional<MatchPair> opt = Optional.ofNullable(service.getActiveMatch(myUserId));

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);

        if (opt.isPresent()) {
            MatchPair m = opt.get();
            resp.put("matchId", m.getId());
            resp.put("status", m.getStatus().name());
            resp.put("userAId", m.getUserAId());
            resp.put("userBId", m.getUserBId());
            resp.put("chatRoomId", m.getChatRoomId());
        } else {
            resp.put("matchId", null);
            resp.put("status", "NONE");
        }
        return ResponseEntity.ok(resp);
    }

}