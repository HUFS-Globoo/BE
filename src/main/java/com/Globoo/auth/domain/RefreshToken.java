package com.Globoo.auth.domain;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name = "refresh_tokens")
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    Long userId; String token; LocalDateTime expiresAt;
}
