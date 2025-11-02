package com.Globoo.auth.repository;


import com.Globoo.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    // RefreshTokenRepository
    Optional<RefreshToken> findByToken(String token);
    void deleteByToken(String token);

}
