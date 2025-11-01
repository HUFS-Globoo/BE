package com.Globoo.common.config;

import com.Globoo.common.security.JwtAuthenticationFilter;
import com.Globoo.common.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
                // API만 보안검사 대상 (Swagger/정적자원은 제외)
                .securityMatcher("/api/**", "/ws/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 회원가입·인증 관련 API 전체 허용
                        .requestMatchers("/api/auth/**").permitAll()

                        // 키워드 / 언어 조회 API 허용 (회원가입 UI용)
                        .requestMatchers("/api/keywords/**", "/api/languages/**").permitAll()
                        //국적용
                        .requestMatchers("/api/countries/**").permitAll()


                        // Swagger 문서 접근 허용
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()

                        // 웹소켓 연결 (매칭용)
                        .requestMatchers("/ws/**").permitAll()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);



        return http.build();
    }
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwt) {
        return new JwtAuthenticationFilter(jwt);
    }
}
