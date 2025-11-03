package com.Globoo.matching.service;

// 채팅 팀의 서비스 및 DTO 임포트
import com.Globoo.chat.dto.ChatRoomCreateReqDto;
import com.Globoo.chat.dto.ChatRoomCreateResDto;
import com.Globoo.chat.service.ChatService;

import com.Globoo.matching.domain.MatchPair;
import com.Globoo.matching.domain.MatchQueue;
import com.Globoo.matching.domain.MatchStatus;
import com.Globoo.matching.repository.MatchPairRepository;
import com.Globoo.matching.repository.MatchQueueRepository;
import com.Globoo.matching.web.MatchingSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final MatchQueueRepository queueRepo;
    private final MatchPairRepository pairRepo;
    private final MatchingSocketHandler socketHandler;

    // [!!! 추가 !!!] 채팅 서비스 주입
    private final ChatService chatService;

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
            MatchQueue userA = waitingUsers.get(0);
            MatchQueue userB = waitingUsers.get(1);

            // 큐 비활성화
            userA.setActive(false);
            userB.setActive(false);
            queueRepo.saveAll(List.of(userA, userB));

            // 새 매칭 생성
            MatchPair match = new MatchPair();
            match.setUserAId(userA.getUserId());
            match.setUserBId(userB.getUserId());
            match.setStatus(MatchStatus.FOUND);
            match.setMatchedAt(LocalDateTime.now());
            match.setMatchedBy("system");
            pairRepo.save(match);

            // 웹소켓 알림
            sendFoundNotification(match);

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
     * ✅ 대기열 이탈 (Controller에서 사용하기 위해 추가)
     */
    @Transactional
    public void leaveQueue(Long userId) {
        // active: true 인 큐 항목을 찾아서
        queueRepo.findByUserIdAndActiveTrue(userId).ifPresent(matchQueue -> {
            // active: false 로 변경
            matchQueue.setActive(false);
            queueRepo.save(matchQueue);
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
     * ✅ 유저 수락 (채팅방 연동 최종 수정)
     */
    @Transactional
    public Map<String, Object> accept(UUID matchId, Long userId) {
        MatchPair match = pairRepo.findById(matchId)
                .orElseThrow(() -> new NoSuchElementException("match not found"));

        if (Objects.equals(match.getUserAId(), userId)) match.setAcceptedA(true);
        if (Objects.equals(match.getUserBId(), userId)) match.setAcceptedB(true);

        // 양쪽 모두 수락 시
        if (Boolean.TRUE.equals(match.getAcceptedA()) && Boolean.TRUE.equals(match.getAcceptedB())) {
            match.setStatus(MatchStatus.ACCEPTED_BOTH);

            // 최종 로직
            Long userA = match.getUserAId();
            Long userB = match.getUserBId();

            // 1. 채팅 서비스 DTO 생성 (userB를 상대로 지정)
            // (ChatRoomCreateReqDto에 @Setter가 추가되었다고 가정)
            ChatRoomCreateReqDto createDto = new ChatRoomCreateReqDto();
            createDto.setParticipantUserId(userB);

            // 2. 채팅 서비스 호출 (userA가 채팅방을 생성)
            ChatRoomCreateResDto responseDto = chatService.createChatRoom(createDto, userA);

            // 3. 반환된 DTO에서 "실제" Long ID 추출
            // [!!! 수정 !!!] try-catch 제거.
            // (ChatRoomCreateResDto에 @Getter가 추가되었다고 가정)
            Long newRoomId = responseDto.getRoomId();

            // 4. MatchPair에 실제 Long ID 저장
            match.setChatRoomId(newRoomId);


        } else {
            match.setStatus(MatchStatus.ACCEPTED_ONE);
        }

        pairRepo.save(match);

        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("state", match.getStatus().name());
        data.put("matchId", match.getId());
        data.put("chatRoomId", match.getChatRoomId()); // 이제 Long 타입 ID 반환

        return data;
    }

    /**
     * ✅ 스킵 & 자동 재매칭 (요구사항 핵심)
     */
    @Transactional
    public Map<String, Object> skipAndRequeue(UUID matchId, Long userId) {
        MatchPair match = pairRepo.findById(matchId)
                .orElseThrow(() -> new NoSuchElementException("match not found"));

        // 매칭 상태 변경 (SKIPPED)
        match.setStatus(MatchStatus.SKIPPED);
        pairRepo.save(match);

        Long userA = match.getUserAId();
        Long userB = match.getUserBId();

        // 두 유저 모두 재큐잉 (요구사항)
        queueRepo.save(new MatchQueue(userA, true, LocalDateTime.now()));
        queueRepo.save(new MatchQueue(userB, true, LocalDateTime.now()));

        // 💡 즉시 재매칭 시도
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
            MatchQueue userA = waitingUsers.get(0);
            MatchQueue userB = waitingUsers.get(1);

            userA.setActive(false);
            userB.setActive(false);
            queueRepo.saveAll(List.of(userA, userB));

            MatchPair newMatch = new MatchPair();
            newMatch.setUserAId(userA.getUserId());
            newMatch.setUserBId(userB.getUserId());
            newMatch.setStatus(MatchStatus.FOUND);
            newMatch.setMatchedAt(LocalDateTime.now());
            newMatch.setMatchedBy("system");
            pairRepo.save(newMatch);

            // 새 매칭 알림
            sendFoundNotification(newMatch);
        }
    }

    /**
     * ✅ WebSocket 알림 (매칭 성사)
     */
    private void sendFoundNotification(MatchPair match) {
        Map<String, Object> payload = Map.of(
                "type", "MATCH_FOUND",
                "matchId", match.getId(),
                "userAId", match.getUserAId(),
                "userBId", match.getUserBId(),
                "status", match.getStatus().name()
        );
        socketHandler.sendToUser(match.getUserAId(), payload);
        socketHandler.sendToUser(match.getUserBId(), payload);
    }
}