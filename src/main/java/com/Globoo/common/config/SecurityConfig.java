package com.Globoo.common.config;

import com.Globoo.common.security.JwtAuthenticationFilter;
import com.Globoo.common.security.JwtTokenProvider;
import com.Globoo.user.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
            "/swagger-resources/**", "/webjars/**"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwt,
                                                           UserRepository userRepository) {
        return new JwtAuthenticationFilter(jwt, userRepository);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtFilter) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // CORS preflight 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Swagger 접근 허용
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()

                        // 정적 업로드 파일 공개 (프로필 이미지 렌더링용)
                        .requestMatchers("/uploads/**").permitAll()

                        // (선택) 루트/정적 인덱스 페이지가 있으면 공개
                        .requestMatchers("/", "/index.html").permitAll()

                        // 인증/온보딩 공개
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/onboarding/**").permitAll()

                        // 공용 조회 API 공개
                        .requestMatchers(
                                "/api/keywords/**",
                                "/api/languages/**",
                                "/api/countries/**"
                        ).permitAll()

                        // STOMP 핸드셰이크 허용
                        .requestMatchers("/ws/**").permitAll()

                        // 그 외는 인증 필요
                        .anyRequest().authenticated()
                )
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())
                .logout(l -> l.disable())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();

        c.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://localhost:5174",
                "http://127.0.0.1:5174",

                // 프론트 배포 주소
                "https://globoo-three.vercel.app",

                // 커스텀 도메인(프론트가 이 도메인에서 호출하는 경우 대비)
                "https://globoo.duckdns.org"

                // 백엔드 자기 자신(koyeb)은 origin으로 의미가 거의 없어서 제거해도 무방
                // "https://instant-gretta-globoo-16d715dd.koyeb.app"
        ));

        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", c);
        return src;
    }
}
