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
import com.Globoo.profile.dto.ProfileCardRes;
import com.Globoo.profile.service.ProfileService;
import com.Globoo.chat.event.ChatSessionEndedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final MatchQueueRepository queueRepo;
    private final MatchPairRepository pairRepo;
    private final MatchingSocketHandler socketHandler;
    private final ChatService chatService;
    private final ProfileService profileService;

    @Transactional
    public Map<String, Object> enterQueue(Long userId) {
        Map<String, Object> result = new HashMap<>();

        if (pairRepo.findActiveMatchByUserId(userId).isPresent()) {
            result.put("success", true);
            result.put("status", "ALREADY_MATCHED");
            return result;
        }

        if (queueRepo.existsByUserIdAndActiveTrue(userId)) {
            result.put("success", true);
            result.put("status", "WAITING");
            return result;
        }

        queueRepo.save(new MatchQueue(userId, true, LocalDateTime.now()));

        var waitingUsers = queueRepo.findTop2ByActiveTrueOrderByEnqueuedAtAsc();

        if (waitingUsers.size() == 2) {
            MatchQueue qA = waitingUsers.get(0);
            MatchQueue qB = waitingUsers.get(1);

            qA.setActive(false);
            qB.setActive(false);
            queueRepo.saveAll(List.of(qA, qB));

            Long user1 = qA.getUserId();
            Long user2 = qB.getUserId();
            long a = Math.min(user1, user2);
            long b = Math.max(user1, user2);

            MatchPair match = new MatchPair();
            match.setUserAId(a);
            match.setUserBId(b);
            match.setStatus(MatchStatus.FOUND);
            match.setMatchedAt(LocalDateTime.now());
            match.setMatchedBy("system");
            pairRepo.save(match);

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

    @Transactional
    public void leaveQueue(Long userId) {
        queueRepo.findByUserIdAndActiveTrue(userId).ifPresent(q -> {
            q.setActive(false);
            queueRepo.save(q);
        });
    }

    @Transactional(readOnly = true)
    public MatchPair getActiveMatch(Long userId) {
        return pairRepo.findActiveMatchByUserId(userId).orElse(null);
    }

    @Transactional
    public Map<String, Object> accept(UUID matchId, Long userId) {

        pairRepo.findActiveMatchByUserId(userId).ifPresent(active -> {
            if (!active.getId().equals(matchId)) {
                throw new IllegalStateException("이미 진행 중인 매칭이 있어 다른 매칭을 수락할 수 없습니다.");
            }
        });

        MatchPair match = pairRepo.findByIdForUpdate(matchId)
                .orElseThrow(() -> new NoSuchElementException("match not found"));

        if (!Objects.equals(match.getUserAId(), userId) &&
                !Objects.equals(match.getUserBId(), userId)) {
            throw new IllegalStateException("이 매칭의 참여자가 아닙니다.");
        }

        if (Objects.equals(match.getUserAId(), userId)) match.setAcceptedA(true);
        if (Objects.equals(match.getUserBId(), userId)) match.setAcceptedB(true);

        if (Boolean.TRUE.equals(match.getAcceptedA()) && Boolean.TRUE.equals(match.getAcceptedB())) {
            match.setStatus(MatchStatus.ACCEPTED_BOTH);

            if (match.getChatRoomId() == null) {
                Long me = match.getUserAId();
                Long other = match.getUserBId();

                ChatRoomCreateReqDto req = new ChatRoomCreateReqDto();
                req.setParticipantUserId(other);

                ChatRoomCreateResDto res = chatService.createChatRoom(req, me);
                Long roomId = res.getRoomId();
                match.setChatRoomId(roomId);

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

    @Transactional
    public Map<String, Object> skipAndRequeue(UUID matchId, Long userId) {
        MatchPair match = pairRepo.findById(matchId)
                .orElseThrow(() -> new NoSuchElementException("match not found"));

        if (!Objects.equals(match.getUserAId(), userId) &&
                !Objects.equals(match.getUserBId(), userId)) {
            throw new IllegalStateException("이 매칭의 참여자가 아닙니다.");
        }

        match.setStatus(MatchStatus.SKIPPED);
        pairRepo.save(match);

        if (!queueRepo.existsByUserIdAndActiveTrue(userId)) {
            queueRepo.save(new MatchQueue(userId, true, LocalDateTime.now()));
        }

        autoRematch();

        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("state", "SKIPPED_AND_REQUEUED");
        return data;
    }

    @Transactional
    public void autoRematch() {
        var waitingUsers = queueRepo.findTop2ByActiveTrueOrderByEnqueuedAtAsc();

        if (waitingUsers.size() == 2) {
            MatchQueue qA = waitingUsers.get(0);
            MatchQueue qB = waitingUsers.get(1);

            qA.setActive(false);
            qB.setActive(false);
            queueRepo.saveAll(List.of(qA, qB));

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

            sendFoundNotification(newMatch);
        }
    }

    @Async
    @EventListener
    public void onChatSessionEnded(ChatSessionEndedEvent event) {
        log.info("[Event] ChatSessionEndedEvent 수신. userId: {}", event.getUserId());
        endChatSession(event.getUserId());
    }

    @Transactional
    public void endChatSession(Long userId) {
        pairRepo.findActiveMatchByUserId(userId).ifPresent(match -> {
            match.setStatus(MatchStatus.NONE);
            pairRepo.save(match);
        });
    }

    private void sendFoundNotification(MatchPair match) {
        Long userAId = match.getUserAId();
        Long userBId = match.getUserBId();

        ProfileCardRes profileA = profileService.getProfileCard(userAId);
        ProfileCardRes profileB = profileService.getProfileCard(userBId);

        Map<String, Object> payloadForA = Map.of(
                "type", "MATCH_FOUND",
                "matchId", match.getId(),
                "myId", userAId,
                "opponentProfile", profileB,
                "status", match.getStatus().name()
        );

        Map<String, Object> payloadForB = Map.of(
                "type", "MATCH_FOUND",
                "matchId", match.getId(),
                "myId", userBId,
                "opponentProfile", profileA,
                "status", match.getStatus().name()
        );

        socketHandler.sendToUser(userAId, payloadForA);
        socketHandler.sendToUser(userBId, payloadForB);
    }

    @Transactional(readOnly = true)
    public boolean isInQueue(Long userId) {
        return queueRepo.existsByUserIdAndActiveTrue(userId);
    }
}