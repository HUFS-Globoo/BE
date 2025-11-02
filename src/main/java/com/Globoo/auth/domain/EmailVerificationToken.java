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
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;

    @ManyToOne(fetch= FetchType.LAZY) @JoinColumn(name="user_id", nullable=false)
    private User user;

    @Column(nullable=false, length=254) private String email;
    @Column(nullable=false, unique=true, length=64) private String token;
    @Column(nullable=false) private LocalDateTime expiresAt;
    private LocalDateTime verifiedAt;
    @Column(nullable=false) private LocalDateTime createdAt;

    @PrePersist void pre(){ createdAt = LocalDateTime.now(); }
    public boolean isExpired(){ return LocalDateTime.now().isAfter(expiresAt); }
    public boolean isUsed(){ return verifiedAt != null; }
}
