package com.Globoo.auth.domain;

import jakarta.persistence.*;
import lombok.*;
import com.Globoo.user.domain.User;
import java.time.LocalDateTime;

// com.Globoo.auth.domain.EmailVerificationToken
@Entity
@Table(name="email_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    // ✅ 인증 전에는 User가 없으므로 nullable=true
    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=true)
    private User user;

    @Column(nullable=false, length=254)
    private String email;

    // 기존처럼 unique 유지 (충돌 방지는 서비스에서 체크)
    @Column(nullable=false, unique=true, length=64)
    private String token;

    @Column(nullable=false)
    private LocalDateTime expiresAt;

    private LocalDateTime verifiedAt;

    @Column(nullable=false)
    private LocalDateTime createdAt;

    //  가입 기본정보 payload(JSON)
    @Column(name="signup_payload", columnDefinition="TEXT")
    private String signupPayload;

    @PrePersist
    void pre(){ createdAt = LocalDateTime.now(); }

    public boolean isExpired(){ return LocalDateTime.now().isAfter(expiresAt); }
    public boolean isUsed(){ return verifiedAt != null; }
}
