package com.Globoo.auth.domain;
import jakarta.persistence.*;
import lombok.*;
import com.Globoo.user.domain.User;
import java.time.LocalDateTime;

// com.Globoo.auth.domain.RefreshToken
@Entity
@Table(name="refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;

    @ManyToOne(fetch= FetchType.LAZY) @JoinColumn(name="user_id", nullable=false)
    private User user;

    @Column(nullable=false, unique=true, length=255) private String token;
    @Column(nullable=false) private LocalDateTime expiresAt;
    private LocalDateTime revokedAt;
    @Column(nullable=false) private LocalDateTime createdAt;

    @PrePersist void pre(){ createdAt = LocalDateTime.now(); }
    public boolean isExpired(){ return LocalDateTime.now().isAfter(expiresAt); }
    public boolean isRevoked(){ return revokedAt != null; }
}
