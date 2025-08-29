package com.moyeorak.auth_service.controller;

import com.moyeorak.auth_service.dto.TokenRefreshRequestDto;
import com.moyeorak.auth_service.dto.TokenResponseDto;
import com.moyeorak.auth_service.dto.UserLoginRequestDto;
import com.moyeorak.auth_service.dto.UserLoginResponseDto;
import com.moyeorak.auth_service.security.CustomUserDetails;
import com.moyeorak.auth_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDto> login(@Valid @RequestBody UserLoginRequestDto dto) {
        log.info("로그인 요청 진입 - email: {}", dto.getEmail());
        UserLoginResponseDto response = authService.login(dto);
        log.info("로그인 완료");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal CustomUserDetails user) {
        log.info("로그아웃 요청 진입 - email: {}", user.getEmail());
        authService.logout(user.getEmail());
        log.info("로그아웃 완료 - email: {}", user.getEmail());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refresh(@RequestBody @Valid TokenRefreshRequestDto dto) {
        log.info("토큰 재발급 요청 진입");
        TokenResponseDto response = authService.refreshAccessToken(dto.getRefreshToken());
        log.info("토큰 재발급 완료");
        return ResponseEntity.ok(response);
    }

}
