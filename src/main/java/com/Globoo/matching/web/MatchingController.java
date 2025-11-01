package com.Globoo.matching.web;

import com.Globoo.matching.domain.MatchPair;
import com.Globoo.matching.service.MatchingService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID; // UUID 임포트 추가

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService service;

    /** 대기열 진입 */
    @PostMapping("/queue")
    public ResponseEntity<?> enterQueue(@RequestBody QueueEnterReq req) {
        // enterQueue는 매칭 결과를 반환할 수 있으므로, 그 결과를 사용하는 것이 좋습니다.
        Map<String, Object> result = service.enterQueue(req.getUserId());

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("data", result); // 서비스의 반환값을 그대로 전달
        return ResponseEntity.ok(resp);
    }

    /** 대기열 취소 */
    @DeleteMapping("/queue")
    public ResponseEntity<?> leaveQueue(@RequestBody UserReq req) {
        service.leaveQueue(req.getUserId());
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "Dequeued");
        return ResponseEntity.ok(resp);
    }

    /** 매칭 수락 */
    // [수정] @PathVariable String matchId -> UUID matchId
    @PostMapping("/{matchId}/accept")
    public ResponseEntity<?> accept(@PathVariable UUID matchId, @RequestBody UserReq req) {
        Map<String, Object> data = service.accept(matchId, req.getUserId());
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("data", data);
        return ResponseEntity.ok(resp);
    }

    /** 다음 상대 찾기(스킵) */
    // [수정] @PathVariable String matchId -> UUID matchId
    @PostMapping("/{matchId}/next")
    public ResponseEntity<?> next(@PathVariable UUID matchId, @RequestBody UserReq req) {
        Map<String, Object> data = service.skipAndRequeue(matchId, req.getUserId());
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("data", data);
        return ResponseEntity.ok(resp);
    }

    /** 현재 진행중 매칭 조회 (헬퍼) */
    @GetMapping("/active/{userId}")
    public ResponseEntity<?> active(@PathVariable Long userId) {
        // [수정] service.activeFor -> service.getActiveMatch
        Optional<MatchPair> opt = Optional.ofNullable(service.getActiveMatch(userId));

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

    // DTOs
    @Data public static class QueueEnterReq { private Long userId; }
    @Data public static class UserReq { private Long userId; }
}