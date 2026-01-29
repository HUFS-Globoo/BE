package com.Globoo.matching.service;

import com.Globoo.chat.dto.ChatRoomCreateReqDto;
import com.Globoo.chat.dto.ChatRoomCreateResDto;
import com.Globoo.chat.event.ChatReadyEvent;
import com.Globoo.chat.event.ChatSessionEndedEvent;
import com.Globoo.chat.event.MatchFoundEvent;
import com.Globoo.chat.service.ChatService;
import com.Globoo.matching.domain.MatchPair;
import com.Globoo.matching.domain.MatchQueue;
import com.Globoo.matching.domain.MatchStatus;
import com.Globoo.matching.repository.MatchPairRepository;
import com.Globoo.matching.repository.MatchQueueRepository;
import com.Globoo.profile.dto.ProfileCardRes;
import com.Globoo.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final MatchQueueRepository queueRepo;
    private final MatchPairRepository pairRepo;
    private final ChatService chatService;
    private final ProfileService profileService;
    private final ApplicationEventPublisher eventPublisher;

    // 매칭 설정 상수
    private static final int LANGUAGE_MATCH_SCORE = 50;
    private static final int KEYWORD_MATCH_SCORE = 10;
    private static final int MBTI_IDEAL_SCORE = 30;
    private static final int MBTI_GOOD_SCORE = 15;
    private static final int DIFFERENT_NATIONALITY_BONUS = 10;
    private static final int SCORE_THRESHOLD = 70;
    private static final int WAIT_TIME_BONUS_PER_10SEC = 5;

    private static final Map<String, List<String>> MBTI_IDEAL_MAP = new HashMap<>();
    private static final Map<String, List<String>> MBTI_GOOD_MAP = new HashMap<>();

    static {
        MBTI_IDEAL_MAP.put("INFP", List.of("ENFJ", "ENTJ"));
        MBTI_IDEAL_MAP.put("ENFP", List.of("INFJ", "INTJ"));
        MBTI_IDEAL_MAP.put("INFJ", List.of("ENFP", "ENTP"));
        MBTI_IDEAL_MAP.put("INTJ", List.of("ENFP", "ENTP"));
        MBTI_IDEAL_MAP.put("ENTP", List.of("INFJ", "INTJ"));
        MBTI_IDEAL_MAP.put("ENTJ", List.of("INFP", "INTP"));
        MBTI_IDEAL_MAP.put("ENFJ", List.of("INFP", "ISFP"));
        MBTI_IDEAL_MAP.put("INTP", List.of("ENTJ", "ESTJ"));
        MBTI_IDEAL_MAP.put("ISFP", List.of("ENFJ", "ESFJ", "ESTJ"));
        MBTI_IDEAL_MAP.put("ISTP", List.of("ESFJ", "ESTJ"));
        MBTI_IDEAL_MAP.put("ISFJ", List.of("ESFP", "ESTP"));
        MBTI_IDEAL_MAP.put("ISTJ", List.of("ESFP", "ESTP"));
        MBTI_IDEAL_MAP.put("ESFP", List.of("ISFJ", "ISTJ"));
        MBTI_IDEAL_MAP.put("ESTP", List.of("ISFJ", "ISTJ"));
        MBTI_IDEAL_MAP.put("ESFJ", List.of("ISFP", "ISTP"));
        MBTI_IDEAL_MAP.put("ESTJ", List.of("INTP", "ISFP", "ISTP"));

        String[] types = {"INFP", "ENFP", "INFJ", "INTJ", "ENTP", "ENTJ", "ENFJ", "INTP", "ISFP", "ISTP", "ISFJ", "ISTJ", "ESFP", "ESTP", "ESFJ", "ESTJ"};
        for (String type : types) MBTI_GOOD_MAP.put(type, List.of(type));
    }

    @Transactional
    public Map<String, Object> enterQueue(Long userId) {
        if (pairRepo.findActiveMatchByUserId(userId).isPresent()) {
            return Map.of("success", true, "status", "ALREADY_MATCHED");
        }

        List<Long> skippedUserIds = pairRepo.findRecentlySkippedUserIds(userId, LocalDateTime.now().minusHours(1));
        ProfileCardRes profile = profileService.getProfileCard(userId);

        String interestsStr = "";
        if (profile.getInterests() != null && !profile.getInterests().isEmpty()) {
            interestsStr = String.join(",", profile.getInterests());
        }

        MatchQueue myNode = MatchQueue.builder()
                .userId(userId)
                .active(true)
                .enqueuedAt(LocalDateTime.now())
                .mbti(profile.mbti())
                .nativeLanguageCode(profile.nativeLanguageCode())
                .preferredLanguageCode(profile.preferredLanguageCode())
                .nationalityCode(profile.nationalityCode())
                .interests(interestsStr)
                .build();

        List<MatchQueue> candidates = queueRepo.findAllByActiveTrueAndUserIdNot(userId);

        if (candidates.isEmpty()) {
            if (!queueRepo.existsByUserIdAndActiveTrue(userId)) {
                queueRepo.save(myNode);
            }
            return Map.of(
                    "success", true,
                    "status", "WAITING",
                    "message", "아직 아무도 없습니댜.. 파트너가 올 때까지 조금만 기다려주세요!"
            );
        }

        MatchQueue bestPartner = null;
        int highestScore = -1;

        for (MatchQueue candidate : candidates) {
            if (skippedUserIds.contains(candidate.getUserId())) continue;

            int score = calculateMatchScore(myNode, candidate);
            int timeBonus = (int) (Duration.between(candidate.getEnqueuedAt(), LocalDateTime.now()).getSeconds() / 10) * WAIT_TIME_BONUS_PER_10SEC;
            int effectiveScore = score + timeBonus;

            if (effectiveScore > highestScore) {
                highestScore = effectiveScore;
                bestPartner = candidate;
            }
        }

        if (bestPartner != null && highestScore >= SCORE_THRESHOLD) {
            bestPartner.setActive(false);
            queueRepo.save(bestPartner);

            MatchPair match = MatchPair.builder()
                    .userAId(Math.min(userId, bestPartner.getUserId()))
                    .userBId(Math.max(userId, bestPartner.getUserId()))
                    .status(MatchStatus.FOUND)
                    .matchedAt(LocalDateTime.now())
                    .matchedBy("adaptive_scoring_system")
                    .build();
            pairRepo.save(match);

            sendFoundNotification(match);
            return Map.of("success", true, "status", "FOUND", "matchId", match.getId());
        }

        if (!queueRepo.existsByUserIdAndActiveTrue(userId)) {
            queueRepo.save(myNode);
        }
        return Map.of("success", true, "status", "WAITING");
    }

    private int calculateMatchScore(MatchQueue my, MatchQueue other) {
        int score = 0;
        if (Objects.equals(my.getPreferredLanguageCode(), other.getNativeLanguageCode())) score += LANGUAGE_MATCH_SCORE;
        if (Objects.equals(other.getPreferredLanguageCode(), my.getNativeLanguageCode())) score += LANGUAGE_MATCH_SCORE;

        if (my.getInterests() != null && other.getInterests() != null) {
            Set<String> mySet = new HashSet<>(Arrays.asList(my.getInterests().split(",")));
            Set<String> otherSet = new HashSet<>(Arrays.asList(other.getInterests().split(",")));
            mySet.retainAll(otherSet);
            score += (mySet.size() * KEYWORD_MATCH_SCORE);
        }

        score += getMbtiCompatibilityScore(my.getMbti(), other.getMbti());
        if (!Objects.equals(my.getNationalityCode(), other.getNationalityCode())) score += DIFFERENT_NATIONALITY_BONUS;

        return score;
    }

    private int getMbtiCompatibilityScore(String my, String other) {
        if (my == null || other == null) return 0;
        if (MBTI_IDEAL_MAP.getOrDefault(my, Collections.emptyList()).contains(other)) return MBTI_IDEAL_SCORE;
        if (MBTI_GOOD_MAP.getOrDefault(my, Collections.emptyList()).contains(other)) return MBTI_GOOD_SCORE;
        return 0;
    }

    @Transactional
    public Map<String, Object> accept(UUID matchId, Long userId) {
        MatchPair match = pairRepo.findByIdForUpdate(matchId).orElseThrow();

        if (Objects.equals(match.getUserAId(), userId)) match.setAcceptedA(true);
        if (Objects.equals(match.getUserBId(), userId)) match.setAcceptedB(true);

        if (Boolean.TRUE.equals(match.getAcceptedA()) && Boolean.TRUE.equals(match.getAcceptedB())) {
            match.setStatus(MatchStatus.ACCEPTED_BOTH);

            if (match.getChatRoomId() == null) {
                ChatRoomCreateReqDto req = new ChatRoomCreateReqDto();
                req.setParticipantUserId(match.getUserAId().equals(userId) ? match.getUserBId() : match.getUserAId());

                ChatRoomCreateResDto res = chatService.createChatRoom(req, userId);
                match.setChatRoomId(res.getRoomId());

                eventPublisher.publishEvent(new ChatReadyEvent(this, match));
            }
        } else {
            match.setStatus(MatchStatus.ACCEPTED_ONE);
        }

        pairRepo.save(match);
        return Map.of(
                "success", true,
                "state", match.getStatus().name(),
                "matchId", match.getId(),
                "chatRoomId", Objects.toString(match.getChatRoomId(), "")
        );
    }

    /**
     * 응답 없는 매칭 정리 (FOUND, ACCEPTED_ONE)
     * - DB status 컬럼이 varchar이므로 native enum cast 쓰지 말고 JPA로 처리
     */
    @Scheduled(fixedRate = 10000)
    @Transactional
    public void cleanupAbandonedMatches() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(20);

        List<MatchStatus> staleStatuses = List.of(MatchStatus.FOUND, MatchStatus.ACCEPTED_ONE);

        pairRepo.findByStatusInAndMatchedAtBefore(staleStatuses, threshold).forEach(m -> {
            log.info("[Cleanup] 매칭 {} 파기 - 응답 시간 초과", m.getId());

            if (Boolean.TRUE.equals(m.getAcceptedA())) reactivateQueue(m.getUserAId());
            if (Boolean.TRUE.equals(m.getAcceptedB())) reactivateQueue(m.getUserBId());

            pairRepo.delete(m);
        });
    }

    private void reactivateQueue(Long userId) {
        queueRepo.findFirstByUserIdOrderByEnqueuedAtDesc(userId).ifPresent(mq -> {
            mq.setActive(true);
            queueRepo.save(mq);
            log.info("[Re-Matching] 유저 {} 기존 대기시간 보존", userId);
        });
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupOldQueueNodes() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(3);

        List<MatchQueue> expired = queueRepo.findAllByActiveTrueAndEnqueuedAtBefore(timeoutThreshold);
        expired.forEach(node -> {
            node.setActive(false);
            log.info("[Queue-Timeout] 유저 {} 대기 시간 초과 제외", node.getUserId());
        });
        queueRepo.saveAll(expired);
    }

    @Async
    @EventListener
    @Transactional
    public void onChatSessionEnded(ChatSessionEndedEvent event) {
        Long roomId = event.getRoomId();
        if (roomId == null) return;

        pairRepo.findLatestByChatRoomId(roomId).ifPresent(m -> {
            m.setStatus(MatchStatus.NONE);
            m.setChatRoomId(null);
            pairRepo.save(m);
        });

        chatService.deleteRoom(roomId);
    }

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void autoMatchingTask() {
        List<MatchQueue> waitingUsers = queueRepo.findAllByActiveTrue();
        if (waitingUsers.size() < 2) return;

        Set<Long> matchedInThisCycle = new HashSet<>();

        for (int i = 0; i < waitingUsers.size(); i++) {
            MatchQueue a = waitingUsers.get(i);
            if (matchedInThisCycle.contains(a.getUserId())) continue;

            for (int j = i + 1; j < waitingUsers.size(); j++) {
                MatchQueue b = waitingUsers.get(j);
                if (matchedInThisCycle.contains(b.getUserId())) continue;

                int baseScore = calculateMatchScore(a, b);

                long secondsA = Duration.between(a.getEnqueuedAt(), LocalDateTime.now()).getSeconds();
                long secondsB = Duration.between(b.getEnqueuedAt(), LocalDateTime.now()).getSeconds();
                int timeBonus = (int) (Math.max(secondsA, secondsB) / 10) * WAIT_TIME_BONUS_PER_10SEC;

                if (baseScore + timeBonus >= SCORE_THRESHOLD) {
                    processAutoMatchSuccess(a, b);
                    matchedInThisCycle.add(a.getUserId());
                    matchedInThisCycle.add(b.getUserId());
                    break;
                }
            }
        }
    }

    private void processAutoMatchSuccess(MatchQueue a, MatchQueue b) {
        a.setActive(false);
        b.setActive(false);
        queueRepo.saveAll(List.of(a, b));

        MatchPair match = MatchPair.builder()
                .userAId(Math.min(a.getUserId(), b.getUserId()))
                .userBId(Math.max(a.getUserId(), b.getUserId()))
                .status(MatchStatus.FOUND)
                .matchedAt(LocalDateTime.now())
                .matchedBy("auto_matching_scheduler")
                .build();
        pairRepo.save(match);

        sendFoundNotification(match);
        log.info("[Auto-Match] 유저 {}와 {} 매칭 성사 (매칭ID: {})", a.getUserId(), b.getUserId(), match.getId());
    }

    private void sendFoundNotification(MatchPair match) {
        ProfileCardRes profileA = profileService.getProfileCard(match.getUserAId());
        ProfileCardRes profileB = profileService.getProfileCard(match.getUserBId());
        eventPublisher.publishEvent(new MatchFoundEvent(this, match, profileA, profileB));
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

    @Transactional(readOnly = true)
    public boolean isInQueue(Long userId) {
        return queueRepo.existsByUserIdAndActiveTrue(userId);
    }

    @Transactional
    public Map<String, Object> skipAndRequeue(UUID matchId, Long userId) {
        MatchPair m = pairRepo.findById(matchId).orElseThrow();
        m.setStatus(MatchStatus.SKIPPED);
        pairRepo.save(m);
        return enterQueue(userId);
    }
}
