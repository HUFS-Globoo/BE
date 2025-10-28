package com.Globoo.common.security;

import com.Globoo.user.domain.User;
import com.Globoo.user.repository.UserRepository;
import com.Globoo.common.error.ErrorCode;
import com.Globoo.common.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;
import java.util.List;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        // JWT 토큰의 subject는 'username'이 아니라 'userId' (Long) 입니다.
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        List<SimpleGrantedAuthority> authorities =
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        // Spring Security의 UserDetails 객체를 반환
        return new org.springframework.security.core.userdetails.User(
                user.getId().toString(), // principal (ID 저장)
                "", // password (JWT 사용하므로 불필요)
                authorities // authorities (권한)
        );
    }
}
