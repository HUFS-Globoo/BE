package com.Globoo.matching.web;

import com.Globoo.matching.domain.MatchPair;
import com.Globoo.matching.service.MatchingService;
// [!!!] User, UserRepository, UserDetails, UsernameNotFoundException 모두 필요 없어짐
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // [!!!] 이것만 남음
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
    // [!!!] UserRepository 주입이 더 이상 필요 없습니다.

    // [!!!] UserDetails를 조회하던 private 메소드도 삭제합니다.
    // private User getUserByDetails(UserDetails userDetails) { ... }


    /** 대기열 진입 */
    @PostMapping("/queue")
    // [!!!] 6. @AuthenticationPrincipal UserDetails userDetails -> Long myUserId로 변경
    public ResponseEntity<?> enterQueue(@AuthenticationPrincipal Long myUserId) {
        // [!!!] 7. user 조회 로직이 필요 없어지고, myUserId가 바로 주입됩니다.
        Map<String, Object> result = service.enterQueue(myUserId);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("data", result);
        return ResponseEntity.ok(resp);
    }

    /** 대기열 취소 */
    @DeleteMapping("/queue")
    public ResponseEntity<?> leaveQueue(@AuthenticationPrincipal Long myUserId) { // [!!!] 6. 변경
        // [!!!] 7. myUserId가 바로 주입됩니다.
        service.leaveQueue(myUserId);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "Dequeued");
        return ResponseEntity.ok(resp);
    }

    /** 매칭 수락 */
    @PostMapping("/{matchId}/accept")
    public ResponseEntity<?> accept(@PathVariable UUID matchId, @AuthenticationPrincipal Long myUserId) { // [!!!] 6. 변경
        // [!!!] 7. myUserId가 바로 주입됩니다.
        Map<String, Object> data = service.accept(matchId, myUserId);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("data", data);
        return ResponseEntity.ok(resp);
    }

    /** 다음 상대 찾기(스킵) */
    @PostMapping("/{matchId}/next")
    public ResponseEntity<?> next(@PathVariable UUID matchId, @AuthenticationPrincipal Long myUserId) { // [!!!] 6. 변경
        // [!!!] 7. myUserId가 바로 주입됩니다.
        Map<String, Object> data = service.skipAndRequeue(matchId, myUserId);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("data", data);
        return ResponseEntity.ok(resp);
    }

    /** 현재 진행중 매칭 조회 (헬퍼) */
    @GetMapping("/active")
    public ResponseEntity<?> active(@AuthenticationPrincipal Long myUserId) { // [!!!] 6. 변경
        // [!!!] 7. myUserId가 바로 주입됩니다.
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