package com.Globoo.auth.service;

import com.Globoo.auth.domain.EmailVerificationToken;
import com.Globoo.auth.repository.EmailVerificationTokenRepository;
import com.Globoo.user.domain.User;
import lombok.RequiredArgsConstructor;
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

    private static final Duration TTL = Duration.ofHours(24);
    private static final Duration RESEND_COOL = Duration.ofMinutes(3);

    /** 6자리 코드 발급 + 메일 전송 */
    @Transactional
    public void issueAndSend(User user) {
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000)); // 000000~999999

        EmailVerificationToken v = EmailVerificationToken.builder()
                .user(user)
                .email(user.getEmail())
                .token(code) // 숫자 코드 저장
                .expiresAt(LocalDateTime.now().plus(TTL))
                .build();

        repo.save(v);
        mail.sendVerificationMail(user.getEmail(), code);
    }

    /** 인증번호 재발송 쿨타임 체크 */
    @Transactional(readOnly = true)
    public void assertResendAllowed(Long userId) {
        repo.findTopByUserIdOrderByCreatedAtDesc(userId).ifPresent(v -> {
            if (v.getCreatedAt().isAfter(LocalDateTime.now().minus(RESEND_COOL))) {
                throw new IllegalStateException("Too frequent resend");
            }
        });
    }

    /** 링크 눌러서 인증하는건에 인증번호로 해서 필요 ㄴㄴ) 
    @Transactional
    public boolean verify(String token) {
        EmailVerificationToken v = repo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("invalid token"));
        if (v.getVerifiedAt() != null) throw new IllegalStateException("already used");
        if (v.getExpiresAt().isBefore(LocalDateTime.now())) throw new IllegalStateException("expired");

        v.setVerifiedAt(LocalDateTime.now());
        v.getUser().setSchoolVerified(true);
        return true;
    }*/

    //이메일 + 6자리 코드 검증
    @Transactional
    public boolean verifyCode(String email, String code) {
        EmailVerificationToken v = repo.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("코드를 먼저 발송하세요."));
        if (v.getVerifiedAt() != null) throw new IllegalStateException("이미 인증되었습니다.");
        if (v.getExpiresAt().isBefore(LocalDateTime.now())) throw new IllegalStateException("코드가 만료되었습니다.");
        if (!v.getToken().equals(code)) throw new IllegalArgumentException("코드가 일치하지 않습니다.");

        v.setVerifiedAt(LocalDateTime.now());
        v.getUser().setSchoolVerified(true);
        return true;
    }
}
