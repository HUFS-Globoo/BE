package com.Globoo.common.config;

import com.Globoo.common.security.JwtAuthenticationFilter;
import com.Globoo.common.security.JwtTokenProvider;
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
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwt) {
        return new JwtAuthenticationFilter(jwt);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtFilter) throws Exception {
        http
                // 이 체인은 아래 경로에만 적용
                .securityMatcher("/api/**", "/ws/**", "/v3/api-docs/**", "/swagger-ui/**")
                .cors(Customizer.withDefaults()) // CORS 허용
                .csrf(csrf -> csrf.disable())    // JWT 환경에서는 CSRF 불필요
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()     // CORS preflight 허용
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()             // Swagger 접근 허용
                        .requestMatchers("/api/auth/**").permitAll()                // 로그인/회원가입 허용
                        .requestMatchers("/api/keywords/**", "/api/languages/**", "/api/countries/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()                      // STOMP 핸드셰이크 허용
                        .anyRequest().authenticated()                               // 나머지는 인증 필요
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

        // 실제 배포 환경 기준으로 허용할 Origin 설정
        c.setAllowedOrigins(List.of(
                "http://localhost:3000",                            // 로컬 프론트 테스트용
                "http://127.0.0.1:3000",                            // 대체 로컬 주소
                "https://instant-gretta-globoo-16d715dd.koyeb.app"  // Koyeb public URL (Swagger / 백엔드)
                // 추후 프론트가 배포되면 아래에 프론트 URL 추가
                // 예: "https://globoo-frontend.vercel.app"
        ));

        c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setAllowCredentials(true);  // JWT Authorization 헤더 사용 가능

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", c);
        return src;
    }
}
