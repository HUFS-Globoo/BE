// src/main/java/com/Globoo/matching/service/MatchingService.java
package com.Globoo.matching.service;

import com.Globoo.chat.dto.ChatRoomCreateReqDto;
import com.Globoo.chat.dto.ChatRoomCreateResDto;
import com.Globoo.chat.service.ChatService;
import com.Globoo.matching.domain.MatchPair;
import com.Globoo.matching.domain.MatchQueue;
import com.Globoo.matching.domain.MatchStatus;
import com.Globoo.matching.repository.MatchPairRepository;
import com.Globoo.matching.repository.MatchQueueRepository;
import com.Globoo.matching.web.MatchingSocketHandler;
// [!!!] 2개의 import 문을 추가합니다.
import com.Globoo.profile.dto.ProfileCardRes;
import com.Globoo.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 랜덤 매칭 → 수락 → 채팅방 연결까지 담당.
 * 핵심:
 * - accept()에서 MatchPair를 PESSIMISTIC_WRITE로 잠그고
 * - 둘 다 수락 시 ChatRoom을 단 한 번만 생성(get-or-create)
 * - 생성된 roomId를 양쪽에 WS로 CHAT_READY 전송
 *
 * 주의:
 * - MatchPair.chatRoomId는 ChatRoom.id와 타입이 맞아야 함(Long).
 */
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final MatchQueueRepository queueRepo;
    private final MatchPairRepository pairRepo;
    private final MatchingSocketHandler socketHandler;
    private final ChatService chatService;
    // [!!!] ProfileService 필드를 추가합니다.
    private final ProfileService profileService;

    /**
     * ✅ 유저가 매칭 큐에 진입
     */
    @Transactional
    public Map<String, Object> enterQueue(Long userId) {
        Map<String, Object> result = new HashMap<>();

        // 이미 큐에 존재하면 중복 방지
        if (queueRepo.existsByUserIdAndActiveTrue(userId)) {
            result.put("success", true);
            result.put("status", "WAITING");
            return result;
        }

        // 대기열에 추가
        queueRepo.save(new MatchQueue(userId, true, LocalDateTime.now()));

        // 다른 유저와 매칭 시도
        var waitingUsers = queueRepo.findTop2ByActiveTrueOrderByEnqueuedAtAsc();

        if (waitingUsers.size() == 2) {
            MatchQueue qA = waitingUsers.get(0);
            MatchQueue qB = waitingUsers.get(1);

            // 큐 비활성화
            qA.setActive(false);
            qB.setActive(false);
            queueRepo.saveAll(List.of(qA, qB));

            // ✅ 유저 ID를 항상 오름차순으로 정렬해서 저장
            Long user1 = qA.getUserId();
            Long user2 = qB.getUserId();
            long a = Math.min(user1, user2);
            long b = Math.max(user1, user2);

            // 새 매칭 생성
            MatchPair match = new MatchPair();
            match.setUserAId(a);
            match.setUserBId(b);
            match.setStatus(MatchStatus.FOUND);
            match.setMatchedAt(LocalDateTime.now());
            match.setMatchedBy("system");
            pairRepo.save(match);

            // 웹소켓 알림 (MATCH_FOUND)
            sendFoundNotification(match); // [!!!] 수정된 메서드가 호출됩니다.

            result.put("success", true);
            result.put("status", "FOUND");
            result.put("matchId", match.getId());
            result.put("userAId", match.getUserAId());
            result.put("userBId", match.getUserBId());
            return result;
        }

        result.put("success", true);
        result.put("status", "WAITING");
        return result;
    }

    /**
     * ✅ 대기열 이탈
     */
    @Transactional
    public void leaveQueue(Long userId) {
        queueRepo.findByUserIdAndActiveTrue(userId).ifPresent(q -> {
            q.setActive(false);
            queueRepo.save(q);
        });
    }

    /**
     * ✅ 현재 매칭 상태 조회
     */
    @Transactional(readOnly = true)
    public MatchPair getActiveMatch(Long userId) {
        return pairRepo.findActiveMatchByUserId(userId).orElse(null);
    }

    /**
     * ✅ 유저 수락 (동시성 안전 + 방 1회 생성 + CHAT_READY 알림)
     */
    @Transactional
    public Map<String, Object> accept(UUID matchId, Long userId) {
        // 동시 수락 경쟁 방지: 행 잠금
        MatchPair match = pairRepo.findByIdForUpdate(matchId)
                .orElseThrow(() -> new NoSuchElementException("match not found"));

        if (Objects.equals(match.getUserAId(), userId)) match.setAcceptedA(true);
        if (Objects.equals(match.getUserBId(), userId)) match.setAcceptedB(true);

        if (Boolean.TRUE.equals(match.getAcceptedA()) && Boolean.TRUE.equals(match.getAcceptedB())) {
            match.setStatus(MatchStatus.ACCEPTED_BOTH);

            // 이미 방이 있으면 재사용, 없으면 이제 한 번만 생성
            if (match.getChatRoomId() == null) {
                Long me = match.getUserAId();
                Long other = match.getUserBId();

                // DTO는 기본 생성자 + 세터 사용
                ChatRoomCreateReqDto req = new ChatRoomCreateReqDto();
                req.setParticipantUserId(other);

                ChatRoomCreateResDto res = chatService.createChatRoom(req, me);
                Long roomId = res.getRoomId(); // ChatRoom PK(Long)
                match.setChatRoomId(roomId);

                // 양쪽에게 채팅 진입 신호
                Map<String, Object> payload = Map.of(
                        "type", "CHAT_READY",
                        "matchId", match.getId(),
                        "roomId", roomId
                );
                socketHandler.sendToUser(match.getUserAId(), payload);
                socketHandler.sendToUser(match.getUserBId(), payload);
            }
        } else {
            match.setStatus(MatchStatus.ACCEPTED_ONE);
        }

        pairRepo.save(match);

        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("state", match.getStatus().name());
        data.put("matchId", match.getId());
        data.put("chatRoomId", match.getChatRoomId());
        return data;
    }

    /**
     * ✅ 스킵 & 자동 재매칭
     */
    @Transactional
    public Map<String, Object> skipAndRequeue(UUID matchId, Long userId) {
        MatchPair match = pairRepo.findById(matchId)
                .orElseThrow(() -> new NoSuchElementException("match not found"));

        match.setStatus(MatchStatus.SKIPPED);
        pairRepo.save(match);

        Long userA = match.getUserAId();
        Long userB = match.getUserBId();

        queueRepo.save(new MatchQueue(userA, true, LocalDateTime.now()));
        queueRepo.save(new MatchQueue(userB, true, LocalDateTime.now()));

        autoRematch();

        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("state", "SKIPPED_AND_REQUEUED");
        return data;
    }

    /**
     * ✅ 자동 재매칭 시도
     */
    @Transactional
    public void autoRematch() {
        var waitingUsers = queueRepo.findTop2ByActiveTrueOrderByEnqueuedAtAsc();

        if (waitingUsers.size() == 2) {
            MatchQueue qA = waitingUsers.get(0);
            MatchQueue qB = waitingUsers.get(1);

            qA.setActive(false);
            qB.setActive(false);
            queueRepo.saveAll(List.of(qA, qB));

            // ✅ 여기서도 userId 정렬해서 저장
            Long user1 = qA.getUserId();
            Long user2 = qB.getUserId();
            long a = Math.min(user1, user2);
            long b = Math.max(user1, user2);

            MatchPair newMatch = new MatchPair();
            newMatch.setUserAId(a);
            newMatch.setUserBId(b);
            newMatch.setStatus(MatchStatus.FOUND);
            newMatch.setMatchedAt(LocalDateTime.now());
            newMatch.setMatchedBy("system");
            pairRepo.save(newMatch);

            sendFoundNotification(newMatch); // 수정된 메서드가 호출됩니다.
        }
    }

    /**
     * ✅ WebSocket 알림 (매칭 성사) - 수정된 버전.
     */
    private void sendFoundNotification(MatchPair match) {
        Long userAId = match.getUserAId();
        Long userBId = match.getUserBId();

        // 1. 양쪽 유저의 프로필 카드를 조회합니다.
        ProfileCardRes profileA = profileService.getProfileCard(userAId);
        ProfileCardRes profileB = profileService.getProfileCard(userBId);

        // 2. UserA에게 보낼 메시지 (상대방 = profileB)
        Map<String, Object> payloadForA = Map.of(
                "type", "MATCH_FOUND",
                "matchId", match.getId(),
                "myId", userAId,
                "opponentProfile", profileB, // A에게는 B의 프로필을 전송
                "status", match.getStatus().name()
        );

        // 3. UserB에게 보낼 메시지 (상대방 = profileA)
        Map<String, Object> payloadForB = Map.of(
                "type", "MATCH_FOUND",
                "matchId", match.getId(),
                "myId", userBId,
                "opponentProfile", profileA, // B에게는 A의 프로필을 전송
                "status", match.getStatus().name()
        );

        // 4. 각자에게 맞는 메시지를 전송
        socketHandler.sendToUser(userAId, payloadForA);
        socketHandler.sendToUser(userBId, payloadForB);
    }
}