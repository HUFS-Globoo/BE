// com/Globoo/auth/repository/EmailVerificationTokenRepository.java
package com.Globoo.auth.repository;

import com.Globoo.auth.domain.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);

    Optional<EmailVerificationToken> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    //가장 최근 이메일발송 레코드
    Optional<EmailVerificationToken> findTopByEmailOrderByCreatedAtDesc(String email);
}
