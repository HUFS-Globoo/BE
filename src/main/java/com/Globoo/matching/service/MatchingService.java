package com.Globoo.matching.service;

import com.Globoo.chat.dto.ChatRoomCreateReqDto;
import com.Globoo.chat.dto.ChatRoomCreateResDto;
import com.Globoo.chat.service.ChatService;
import com.Globoo.chat.event.ChatReadyEvent;
import com.Globoo.chat.event.MatchFoundEvent;
import com.Globoo.chat.event.ChatSessionEndedEvent;
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

    //  매칭 가중치 및 설정 상수
    private static final int LANGUAGE_MATCH_SCORE = 50;      // 학습 언어와 상대 모국어 일치 시
    private static final int KEYWORD_MATCH_SCORE = 10;       // 관심사 키워드 1개당 점수
    private static final int MBTI_IDEAL_SCORE = 30;          // MBTI 천생연분 궁합
    private static final int MBTI_GOOD_SCORE = 15;           // MBTI 좋은 궁합
    private static final int DIFFERENT_NATIONALITY_BONUS = 10; // 글로벌 매칭을 위한 국적 다름 보너스
    private static final int SCORE_THRESHOLD = 70;           // 매칭 성사 기준 점수
    private static final int WAIT_TIME_BONUS_PER_10SEC = 2;  // 10초 대기당 가산 점수 (Adaptive Matching)

    // MBTI 궁합 맵 데이터
    private static final Map<String, List<String>> MBTI_IDEAL_MAP = new HashMap<>();
    private static final Map<String, List<String>> MBTI_GOOD_MAP = new HashMap<>();

    static {
        // 천생연분 데이터 세팅
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

        // 동일 유형은 '좋은 궁합'으로 분류
        String[] types = {"INFP", "ENFP", "INFJ", "INTJ", "ENTP", "ENTJ", "ENFJ", "INTP", "ISFP", "ISTP", "ISFJ", "ISTJ", "ESFP", "ESTP", "ESFJ", "ESTJ"};
        for (String type : types) MBTI_GOOD_MAP.put(type, List.of(type));
    }

    /**
     *  대기열 진입 및 실시간 가중치 매칭 실행
     */
    @Transactional
    public Map<String, Object> enterQueue(Long userId) {
        // 1. 현재 진행 중인 매칭이 있으면 중단
        if (pairRepo.findActiveMatchByUserId(userId).isPresent()) {
            return Map.of("success", true, "status", "ALREADY_MATCHED");
        }

        // 2. 블랙리스트 확인: 1시간 이내에 내가 스킵했거나 나를 스킵한 유저는 제외
        List<Long> skippedUserIds = pairRepo.findRecentlySkippedUserIds(userId, LocalDateTime.now().minusHours(1));

        // 3. 내 프로필 정보를 바탕으로 매칭 노드 생성
        ProfileCardRes profile = profileService.getProfileCard(userId);

        List<String> keywords = new ArrayList<>();
        if (profile.personalityKeywords() != null) keywords.addAll(profile.personalityKeywords());
        if (profile.hobbyKeywords() != null) keywords.addAll(profile.hobbyKeywords());
        if (profile.topicKeywords() != null) keywords.addAll(profile.topicKeywords());

        MatchQueue myNode = MatchQueue.builder()
                .userId(userId)
                .mbti(profile.mbti()) // getMbti() -> mbti()
                .nativeLanguageCode(profile.nativeLanguageCode()) // getNativeLanguageCode() -> nativeLanguageCode()
                .preferredLanguageCode(profile.preferredLanguageCode())
                .nationalityCode(profile.nationalityCode())
                .interests(String.join(",", keywords))
                .build();

        // 4. 대기열 내 다른 유저들과 점수 비교
        List<MatchQueue> candidates = queueRepo.findAllByActiveTrueAndUserIdNot(userId);
        MatchQueue bestPartner = null;
        int highestScore = -1;

        for (MatchQueue candidate : candidates) {
            // 블랙리스트 유저는 매칭 후보에서 건너뜀
            if (skippedUserIds.contains(candidate.getUserId())) continue;

            // 기본 프로필 점수 계산
            int score = calculateMatchScore(myNode, candidate);

            // 대기 시간 보너스 적용 (10초당 가산점) -> 오래 기다린 사람일수록 문턱이 낮아짐
            int timeBonus = (int) (Duration.between(candidate.getEnqueuedAt(), LocalDateTime.now()).getSeconds() / 10) * WAIT_TIME_BONUS_PER_10SEC;
            int effectiveScore = score + timeBonus;

            // 최고 점수 파트너 갱신
            if (effectiveScore > highestScore) {
                highestScore = effectiveScore;
                bestPartner = candidate;
            }
        }

        // 5. 점수가 기준치를 넘으면 즉시 매칭 성사
        if (bestPartner != null && highestScore >= SCORE_THRESHOLD) {
            bestPartner.setActive(false); // 파트너 큐 상태 비활성화
            queueRepo.save(bestPartner);

            MatchPair match = MatchPair.builder()
                    .userAId(Math.min(userId, bestPartner.getUserId()))
                    .userBId(Math.max(userId, bestPartner.getUserId()))
                    .status(MatchStatus.FOUND)
                    .matchedAt(LocalDateTime.now())
                    .matchedBy("adaptive_scoring_system")
                    .build();
            pairRepo.save(match);

            // 매칭 알림 전송 (Event-Driven)
            sendFoundNotification(match);
            return Map.of("success", true, "status", "FOUND", "matchId", match.getId());
        }

        // 6. 적절한 파트너가 없으면 대기열에 본인 등록
        if (!queueRepo.existsByUserIdAndActiveTrue(userId)) {
            myNode.setActive(true);
            myNode.setEnqueuedAt(LocalDateTime.now());
            queueRepo.save(myNode);
        }
        return Map.of("success", true, "status", "WAITING");
    }

    /**
     *  상세 점수 계산 (언어, 관심사, MBTI, 국적)
     */
    private int calculateMatchScore(MatchQueue my, MatchQueue other) {
        int score = 0;

        // 1. 언어 매칭: 내가 배우고 싶은 언어가 상대의 모국어일 때
        if (Objects.equals(my.getPreferredLanguageCode(), other.getNativeLanguageCode())) score += LANGUAGE_MATCH_SCORE;
        if (Objects.equals(other.getPreferredLanguageCode(), my.getNativeLanguageCode())) score += LANGUAGE_MATCH_SCORE;

        // 2. 관심사 키워드 매칭: 겹치는 키워드 개수당 점수 가산
        if (my.getInterests() != null && other.getInterests() != null) {
            Set<String> mySet = new HashSet<>(Arrays.asList(my.getInterests().split(",")));
            Set<String> otherSet = new HashSet<>(Arrays.asList(other.getInterests().split(",")));
            mySet.retainAll(otherSet); // 교집합 산출
            score += (mySet.size() * KEYWORD_MATCH_SCORE);
        }

        // 3. MBTI 궁합 점수 적용
        score += getMbtiCompatibilityScore(my.getMbti(), other.getMbti());

        // 4. 국적 보너스: 서로 국적이 다를 경우 글로벌 매칭 가산점
        if (!Objects.equals(my.getNationalityCode(), other.getNationalityCode())) score += DIFFERENT_NATIONALITY_BONUS;

        return score;
    }

    /**
     *  MBTI 궁합 점수 산출 헬퍼
     */
    private int getMbtiCompatibilityScore(String my, String other) {
        if (my == null || other == null) return 0;
        if (MBTI_IDEAL_MAP.getOrDefault(my, Collections.emptyList()).contains(other)) return MBTI_IDEAL_SCORE;
        if (MBTI_GOOD_MAP.getOrDefault(my, Collections.emptyList()).contains(other)) return MBTI_GOOD_SCORE;
        return 0;
    }

    /**
     *  매칭 수락 처리 및 채팅방 생성
     */
    @Transactional
    public Map<String, Object> accept(UUID matchId, Long userId) {
        MatchPair match = pairRepo.findByIdForUpdate(matchId).orElseThrow();
        if (Objects.equals(match.getUserAId(), userId)) match.setAcceptedA(true);
        if (Objects.equals(match.getUserBId(), userId)) match.setAcceptedB(true);

        // 둘 다 수락한 경우
        if (Boolean.TRUE.equals(match.getAcceptedA()) && Boolean.TRUE.equals(match.getAcceptedB())) {
            match.setStatus(MatchStatus.ACCEPTED_BOTH);
            if (match.getChatRoomId() == null) {
                ChatRoomCreateReqDto req = new ChatRoomCreateReqDto();
                req.setParticipantUserId(match.getUserAId().equals(userId) ? match.getUserBId() : match.getUserAId());

                // 실제 채팅방 생성 서비스 호출
                ChatRoomCreateResDto res = chatService.createChatRoom(req, userId);
                match.setChatRoomId(res.getRoomId());

                // 채팅방 준비 완료 이벤트 발행
                eventPublisher.publishEvent(new ChatReadyEvent(this, match));
            }
        } else {
            match.setStatus(MatchStatus.ACCEPTED_ONE);
        }
        pairRepo.save(match);
        return Map.of("success", true, "state", match.getStatus().name(), "matchId", match.getId(), "chatRoomId", Objects.toString(match.getChatRoomId(), ""));
    }

    /**
     *  응답 없는 매칭 클린업 (10초마다 실행)
     * FOUND 상태에서 아무도 수락하지 않거나, 한 명만 수락하고 잠수 타는 경우 방지
     */
    @Scheduled(fixedRate = 10000)
    @Transactional
    public void cleanupAbandonedMatches() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(20); // 20초간 무응답 시
        List<MatchStatus> staleStatuses = List.of(MatchStatus.FOUND, MatchStatus.ACCEPTED_ONE);

        pairRepo.findStaleMatches(staleStatuses, threshold).forEach(m -> {
            m.setStatus(MatchStatus.NONE); // 매칭 무효화
            pairRepo.save(m);
            log.info("[Cleanup] 응답 시간 초과로 매칭 정리: {}", m.getId());
        });
    }

    /**
     *  채팅 세션 종료 시 매칭 상태를 NONE으로 리셋
     */
    @Async @EventListener
    public void onChatSessionEnded(ChatSessionEndedEvent event) {
        pairRepo.findActiveMatchByUserId(event.getUserId()).ifPresent(m -> {
            m.setStatus(MatchStatus.NONE);
            pairRepo.save(m);
        });
    }

    /**
     *  매칭 발견 알림 이벤트 발행
     */
    private void sendFoundNotification(MatchPair match) {
        ProfileCardRes profileA = profileService.getProfileCard(match.getUserAId());
        ProfileCardRes profileB = profileService.getProfileCard(match.getUserBId());
        eventPublisher.publishEvent(new MatchFoundEvent(this, match, profileA, profileB));
    }

    // --- 기타 헬퍼 메서드 ---
    @Transactional public void leaveQueue(Long userId) { queueRepo.findByUserIdAndActiveTrue(userId).ifPresent(q -> { q.setActive(false); queueRepo.save(q); }); }
    @Transactional(readOnly = true) public MatchPair getActiveMatch(Long userId) { return pairRepo.findActiveMatchByUserId(userId).orElse(null); }
    @Transactional(readOnly = true) public boolean isInQueue(Long userId) { return queueRepo.existsByUserIdAndActiveTrue(userId); }

    /**
     *  다음 상대 찾기 (현재 매칭 스킵 후 다시 큐에 진입)
     */
    @Transactional public Map<String, Object> skipAndRequeue(UUID matchId, Long userId) {
        MatchPair m = pairRepo.findById(matchId).orElseThrow();
        m.setStatus(MatchStatus.SKIPPED);
        pairRepo.save(m);
        return enterQueue(userId); // 다시 대기열 로직 수행
    }
}