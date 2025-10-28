package com.Globoo.auth.service;

import com.Globoo.auth.dto.AuthLoginReqDto;
import com.Globoo.auth.dto.AuthLoginResDto;
import com.Globoo.common.security.JwtTokenProvider;
import com.Globoo.user.domain.User;
import com.Globoo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// (실제로는 PasswordEncoder, UserNotFoundException 등 필요)

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    // private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AuthLoginResDto login(AuthLoginReqDto dto) {
        // 1. (실제 로직) 이메일로 User를 찾습니다.
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다.")); // (임시 예외)

        // 2. (실제 로직) 비밀번호를 비교합니다.
        // if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
        //     throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        // }

        // 3. (테스트용) User ID로 토큰을 생성합니다. (User ID 1이라고 가정)
        // User user = userRepository.findById(1L).get(); // (테스트용 하드코딩)
        String token = jwtTokenProvider.createToken(user.getId().toString());

        return new AuthLoginResDto(token);
    }
}