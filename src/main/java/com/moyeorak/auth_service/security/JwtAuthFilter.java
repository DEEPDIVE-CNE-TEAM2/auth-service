package com.moyeorak.auth_service.security;

// 요청이 오면 JWT 토큰 유효한지 확인 후 인증 처리
import com.moyeorak.auth_service.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    // 인증 불필요한 경로 → 필터 스킵
    private static final List<String> PUBLIC_URLS = List.of(
            "/internal/users/**",
            "/api/users/signup",
            "/api/auth/login",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    );

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // 공개 경로는 JWT 검증 스킵
        if (PUBLIC_URLS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path))) {
            filterChain.doFilter(request, response);
            return;
        }

        // Authorization 헤더 추출
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            log.warn("Authorization 헤더가 없거나 잘못된 형식: {}", header);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증 토큰이 없습니다.");
            return;
        }

        String token = header.substring(7);

        try {
            // 토큰 검증 및 사용자 인증
            String email = jwtProvider.getEmail(token);
            String role = jwtProvider.getRole(token);

            if (email != null && role != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                var user = userRepository.findByEmail(email).orElseThrow(() -> {
                    log.warn("❌ 사용자 DB 조회 실패: {}", email);
                    return new RuntimeException("존재하지 않는 사용자입니다.");
                });

                var authority = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());
                var auth = new UsernamePasswordAuthenticationToken(
                        new CustomUserDetails(user.getId(), user.getEmail(), user.getPassword(), List.of(authority)),
                        null,
                        List.of(authority)
                );

                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("✅ JWT 인증 성공 - 사용자: {}, 권한: {}", email, role);
            }

        } catch (ExpiredJwtException e) {
            log.warn("❌ 토큰 만료: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰이 만료되었습니다.");
            return;
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            log.warn("❌ 잘못된 토큰: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다.");
            return;
        } catch (Exception e) {
            log.error("❌ JWT 인증 중 알 수 없는 오류", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "JWT 인증 오류");
            return;
        }

        // 다음 필터로 진행
        filterChain.doFilter(request, response);
    }
}