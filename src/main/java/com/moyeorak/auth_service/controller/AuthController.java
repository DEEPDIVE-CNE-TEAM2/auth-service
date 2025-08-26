package com.moyeorak.auth_service.controller;

import com.moyeorak.auth_service.dto.TokenRefreshRequestDto;
import com.moyeorak.auth_service.dto.TokenResponseDto;
import com.moyeorak.auth_service.dto.UserLoginRequestDto;
import com.moyeorak.auth_service.dto.UserLoginResponseDto;
import com.moyeorak.auth_service.security.CustomUserDetails;
import com.moyeorak.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDto> login(@Valid @RequestBody UserLoginRequestDto dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal CustomUserDetails user) {
        authService.logout(user.getEmail());
        return ResponseEntity.ok().build();
    }

    // 액세스 토큰 재발급
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refresh(@RequestBody @Valid TokenRefreshRequestDto dto) {
        return ResponseEntity.ok(authService.refreshAccessToken(dto.getRefreshToken()));
    }

}
