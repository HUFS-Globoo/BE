package com.Globoo.auth.repository;


import com.Globoo.auth.domain.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> { }
