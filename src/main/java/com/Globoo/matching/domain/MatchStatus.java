package com.Globoo.matching.domain;

public enum MatchStatus {
    WAITING,       // 대기 중
    FOUND,         // 매칭 상대 발견 (수락 대기)
    SKIPPED,       // 스킵됨
    ACCEPTED_ONE,  // 한 명만 수락함
    ACCEPTED_BOTH, // 둘 다 수락함 (채팅 시작)
    NONE           // 매칭 종료 또는 무효화
}

// waiting 이 현재 사용되지 않는 문제