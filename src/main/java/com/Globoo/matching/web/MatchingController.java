// src/main/java/com/Globoo/matching/web/MatchingController.java
package com.Globoo.matching.web;

import com.Globoo.matching.domain.MatchPair;
import com.Globoo.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<?> enterQueue(@AuthenticationPrincipal Long myUserId) {
        Map<String, Object> result = service.enterQueue(myUserId);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("data", result);
        return ResponseEntity.ok(resp);
    }

    /** 대기열 취소 */
    @DeleteMapping("/queue")
    public ResponseEntity<?> leaveQueue(@AuthenticationPrincipal Long myUserId) {
        service.leaveQueue(myUserId);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "Dequeued");
        return ResponseEntity.ok(resp);
    }

    /** 매칭 수락 */
    @PostMapping("/{matchId}/accept")
    public ResponseEntity<?> accept(@PathVariable UUID matchId,
                                    @AuthenticationPrincipal Long myUserId) {

        Map<String, Object> data = service.accept(matchId, myUserId);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("data", data);
        return ResponseEntity.ok(resp);
    }

    /** 다음 상대 찾기(스킵) */
    @PostMapping("/{matchId}/next")
    public ResponseEntity<?> next(@PathVariable UUID matchId,
                                  @AuthenticationPrincipal Long myUserId) {

        Map<String, Object> data = service.skipAndRequeue(matchId, myUserId);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("data", data);
        return ResponseEntity.ok(resp);
    }

    /** 현재 진행중 매칭 조회 (헬퍼) */
    @GetMapping("/active")
    public ResponseEntity<?> active(@AuthenticationPrincipal Long myUserId) {
        Optional<MatchPair> opt = Optional.ofNullable(service.getActiveMatch(myUserId));

        Map<String, Object> data = new LinkedHashMap<>();

        if (opt.isPresent()) {
            MatchPair m = opt.get();
            data.put("matchId", m.getId());
            data.put("status", m.getStatus().name());
            data.put("userAId", m.getUserAId());
            data.put("userBId", m.getUserBId());
            data.put("chatRoomId", m.getChatRoomId());
        } else {
            data.put("matchId", null);
            data.put("status", "NONE");
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("data", data);
        return ResponseEntity.ok(resp);
    }
}
