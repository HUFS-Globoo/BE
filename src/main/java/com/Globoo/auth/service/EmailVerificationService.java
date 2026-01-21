package com.Globoo.auth.service;

import com.Globoo.auth.domain.EmailVerificationToken;
import com.Globoo.auth.dto.PendingSignupPayload;
import com.Globoo.auth.dto.SignupStep1Req;
import com.Globoo.auth.repository.EmailVerificationTokenRepository;
import com.Globoo.common.error.AuthException;
import com.Globoo.common.error.ErrorCode;
import com.Globoo.profile.store.ProfileRepository;
import com.Globoo.user.domain.Campus;
import com.Globoo.user.domain.Profile;
import com.Globoo.user.domain.User;
import com.Globoo.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final EmailVerificationTokenRepository repo;
    private final MailService mail;

    private final ObjectMapper objectMapper;
    private final PasswordEncoder encoder;

    private final UserRepository userRepo;
    private final ProfileRepository profileRepo;

    private static final Duration TTL = Duration.ofHours(24);
    private static final Duration RESEND_COOL = Duration.ofMinutes(3);

    /** 6자리 코드 발급 + 메일 전송 (가입대기: User 생성 X) */
    @Transactional
    public void issueAndSend(SignupStep1Req dto) {
        String code = nextUniqueCode();

        PendingSignupPayload payload = new PendingSignupPayload(
                dto.email(),
                dto.username(),
                encoder.encode(dto.password()),
                dto.name(),
                dto.phoneNumber(),
                dto.nickname(),
                dto.birthDate(),
                dto.gender()
        );

        String payloadJson = toJson(payload);

        EmailVerificationToken v = EmailVerificationToken.builder()
                .user(null)
                .email(dto.email())
                .token(code)
                .expiresAt(LocalDateTime.now().plus(TTL))
                .signupPayload(payloadJson)
                .build();

        repo.save(v);
        mail.sendVerificationMail(dto.email(), code);
    }

    /** 재발송 쿨타임 체크 (email 기준) */
    @Transactional(readOnly = true)
    public void assertResendAllowed(String email) {
        repo.findTopByEmailOrderByCreatedAtDesc(email).ifPresent(v -> {
            if (v.getCreatedAt().isAfter(LocalDateTime.now().minus(RESEND_COOL))) {
                throw new AuthException(ErrorCode.TOO_FREQUENT_RESEND);
            }
        });
    }

    /** 재발송: 최근 payload 그대로 새 코드 발급 + 새 레코드 저장 + 메일 전송 */
    @Transactional
    public void resendCode(String email) {
        EmailVerificationToken last = repo.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new AuthException(ErrorCode.VERIFICATION_REQUIRED));

        if (last.getVerifiedAt() != null) {
            throw new AuthException(ErrorCode.FORBIDDEN_ACCESS);
        }

        String code = nextUniqueCode();

        EmailVerificationToken v = EmailVerificationToken.builder()
                .user(null)
                .email(email)
                .token(code)
                .expiresAt(LocalDateTime.now().plus(TTL))
                .signupPayload(last.getSignupPayload())
                .build();

        repo.save(v);
        mail.sendVerificationMail(email, code);
    }

    /**
     * Step2: 이메일 + 6자리 코드 + 캠퍼스 검증 (여기서 User/Profile 생성)
     * @return 생성된 userId
     */
    @Transactional
    public Long verifyCodeAndCreateUser(String email, String code, Campus campus) {
        // ✅ 2차 방어: 컨트롤러/DTO 검증 누락 or 내부 호출 대비
        if (campus == null) {
            throw new AuthException(ErrorCode.CAMPUS_REQUIRED);
        }

        EmailVerificationToken v = repo.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new AuthException(ErrorCode.VERIFICATION_REQUIRED));

        if (v.getVerifiedAt() != null) throw new AuthException(ErrorCode.FORBIDDEN_ACCESS);
        if (v.getExpiresAt().isBefore(LocalDateTime.now())) throw new AuthException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        if (!v.getToken().equals(code)) throw new AuthException(ErrorCode.VERIFICATION_CODE_MISMATCH);

        PendingSignupPayload payload = fromJson(v.getSignupPayload(), PendingSignupPayload.class);

        // 인증 성공 시점 최종 중복 방어
        if (userRepo.existsByEmail(payload.email())) throw new AuthException(ErrorCode.EMAIL_ALREADY_EXISTS);
        if (userRepo.existsByUsername(payload.username())) throw new AuthException(ErrorCode.USERNAME_ALREADY_EXISTS);
        if (profileRepo.existsByNickname(payload.nickname())) throw new AuthException(ErrorCode.NICKNAME_ALREADY_EXISTS);

        User u = userRepo.save(User.builder()
                .email(payload.email())
                .username(payload.username())
                .password(payload.encodedPassword())
                .name(payload.name())
                .phoneNumber(payload.phoneNumber())
                .schoolVerified(true)
                .build());

        profileRepo.save(Profile.builder()
                .user(u)
                .nickname(payload.nickname())
                .birthDate(payload.birthDate())
                .gender(payload.gender())
                .campus(campus)   // ✅ Step2에서 받은 campus로 저장
                .build());

        v.setVerifiedAt(LocalDateTime.now());
        v.setUser(u);

        return u.getId();
    }

    // ---------- utils ----------

    private String nextUniqueCode() {
        for (int i = 0; i < 10; i++) {
            String code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
            if (repo.findByToken(code).isEmpty()) return code;
        }
        throw new AuthException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new AuthException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private <T> T fromJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new AuthException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
