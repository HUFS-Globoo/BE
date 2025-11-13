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

    /**
     * ✅ 현재 진행중 매칭/대기 상태 조회
     *
     * - active 매칭이 있으면: FOUND / ACCEPTED_ONE / ACCEPTED_BOTH
     * - active 매칭은 없지만 큐에 있으면: WAITING
     * - 아무 것도 없으면: NONE
     */
    @GetMapping("/active")
    public ResponseEntity<?> active(@AuthenticationPrincipal Long myUserId) {
        MatchPair activeMatch = service.getActiveMatch(myUserId);

        Map<String, Object> data = new LinkedHashMap<>();

        if (activeMatch != null) {
            // 현재 진행중 매칭이 있는 경우
            data.put("matchId", activeMatch.getId());
            data.put("status", activeMatch.getStatus().name()); // FOUND / ACCEPTED_ONE / ACCEPTED_BOTH
            data.put("userAId", activeMatch.getUserAId());
            data.put("userBId", activeMatch.getUserBId());
            data.put("chatRoomId", activeMatch.getChatRoomId());
        } else if (service.isInQueue(myUserId)) {
            // 매칭은 아직 안 만들어졌지만 큐에는 올라가 있는 경우
            data.put("matchId", null);
            data.put("status", "WAITING");
        } else {
            // 완전 아무 상태도 아닌 경우
            data.put("matchId", null);
            data.put("status", "NONE");
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("data", data);
        return ResponseEntity.ok(resp);
    }
}
