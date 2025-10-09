package com.Globoo.auth.domain;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name = "email_verifications")
public class EmailVerificationToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    Long userId; String email; String token; LocalDateTime expiresAt;
}
