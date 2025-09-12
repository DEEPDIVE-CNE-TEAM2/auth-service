package com.moyeorak.auth_service.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // JWT 사용하니까 CSRF, Form 로그인, HTTP Basic 다 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/internal/users/**",
                                "/api/users/signup",
                                "/api/auth/login",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/auth/.well-known/**",
                                "/actuator/**",
                                "/health"
                        ).permitAll()
                        //.anyRequest().authenticated()
                        // 임시 전체 허용 나중에 삭제
                        .anyRequest().permitAll()
                )

                // 인증 실패 / 권한 없음 처리
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            // 인증 실패 → 401
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다.");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            // 권한 없음 → 403
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "접근 권한이 없습니다.");
                        })
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:8080",
                "http://localhost:5173",
                "http://127.0.0.1:5173",
		"https://api.moyeorak.cloud",   // API Gateway/ALB 도메인
    		"https://moyeorak.cloud" 
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type",
                "X-User-Id", "X-User-Role", "X-User-Region-Id"));
        config.setExposedHeaders(List.of("Content-Disposition", "Location"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
