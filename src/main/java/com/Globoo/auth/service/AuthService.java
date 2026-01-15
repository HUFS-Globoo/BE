package com.Globoo.auth.service;

import com.Globoo.auth.domain.RefreshToken;
import com.Globoo.auth.dto.*;
import com.Globoo.auth.repository.RefreshTokenRepository;
import com.Globoo.common.error.AuthException;
import com.Globoo.common.error.ErrorCode;
import com.Globoo.common.security.JwtTokenProvider;
import com.Globoo.profile.store.ProfileRepository;
import com.Globoo.user.domain.User;
import com.Globoo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final RefreshTokenRepository rtRepo;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwt;
    private final EmailVerificationService emailVerif;

    // 닉네임 중복 체크용
    private final ProfileRepository profileRepo;

    @Transactional
    public SignupRes signup(SignupReq dto) {
        if (userRepo.existsByEmail(dto.email())) throw new AuthException(ErrorCode.EMAIL_ALREADY_EXISTS);
        if (userRepo.existsByUsername(dto.username())) throw new AuthException(ErrorCode.USERNAME_ALREADY_EXISTS);
        if (profileRepo.existsByNickname(dto.nickname())) throw new AuthException(ErrorCode.NICKNAME_ALREADY_EXISTS);

        // ✅ 유저 생성은 verify-code 성공 시점에만!
        emailVerif.issueAndSend(dto);

        return new SignupRes(null, dto.email(), dto.username(), dto.nickname(), false);
    }

    @Transactional
    public OkRes resend(ResendReq req) {
        emailVerif.assertResendAllowed(req.email());
        emailVerif.resendCode(req.email());
        return new OkRes(true);
    }

    @Transactional
    public TokenRes login(LoginReq req) {
        User u = userRepo.findByEmail(req.email())
                .orElseThrow(() -> new AuthException(ErrorCode.INVALID_CREDENTIALS));

        if (!encoder.matches(req.password(), u.getPassword())) {
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!u.isSchoolVerified()) {
            throw new AuthException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        String access = jwt.createAccessToken(u.getId(), u.getEmail());
        String refresh = UUID.randomUUID().toString();

        rtRepo.save(RefreshToken.builder()
                .user(u)
                .token(refresh)
                .expiresAt(LocalDateTime.now().plusDays(14))
                .build());

        return new TokenRes(
                access,
                refresh,
                "Bearer",
                jwt.getAccessTokenValiditySec(),
                u.getId()
        );
    }

    @Transactional
    public TokenRes refresh(String refreshToken) {
        RefreshToken rt = rtRepo.findByToken(refreshToken)
                .orElseThrow(() -> new AuthException(ErrorCode.UNAUTHORIZED));

        if (rt.isExpired() || rt.isRevoked()) {
            throw new AuthException(ErrorCode.UNAUTHORIZED);
        }

        if (!rt.getUser().isSchoolVerified()) {
            throw new AuthException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        String access = jwt.createAccessToken(rt.getUser().getId(), rt.getUser().getEmail());

        return new TokenRes(
                access,
                refreshToken,
                "Bearer",
                jwt.getAccessTokenValiditySec(),
                rt.getUser().getId()
        );
    }

    @Transactional
    public OkRes logout(String refreshToken) {
        rtRepo.findByToken(refreshToken)
                .ifPresent(t -> t.setRevokedAt(LocalDateTime.now()));
        return new OkRes(true);
    }
}
