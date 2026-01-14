package com.Globoo.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),

    // Auth (기존)
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // Auth - Signup / Login / Verification (✅ 추가)
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "학교 인증이 완료되지 않은 계정입니다."),
    VERIFICATION_REQUIRED(HttpStatus.BAD_REQUEST, "인증 코드를 먼저 발송하세요."),
    VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "코드가 만료되었습니다."),
    VERIFICATION_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "코드가 일치하지 않습니다."),
    TOO_FREQUENT_RESEND(HttpStatus.TOO_MANY_REQUESTS, "인증번호 재발송은 잠시 후 다시 시도해주세요."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    // Chat
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),
    ALREADY_PARTICIPANT(HttpStatus.CONFLICT, "이미 채팅방에 참여중인 사용자입니다."),
    NOT_CHAT_PARTICIPANT(HttpStatus.FORBIDDEN, "채팅방 참여자가 아닙니다.");

    private final HttpStatus status;
    private final String message;
}
