package com.moyeorak.auth_service.controller;

import com.moyeorak.auth_service.dto.*;
import com.moyeorak.auth_service.service.AuthService;
import com.moyeorak.auth_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.moyeorak.auth_service.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;


    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<UserSignupResponseDto> signup(@Valid @RequestBody UserSignupRequestDto dto) {
        return ResponseEntity.ok(userService.signup(dto));
    }

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


    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyInfo(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(userService.getMyInfo(user.getEmail()));
    }

    // 내 정보 수정
    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateMyInfo(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody @Valid UserUpdateRequestDto dto
    ) {
        return ResponseEntity.ok(userService.updateUserInfo(user.getEmail(), dto));
    }

    // 비밀번호 변경
    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody @Valid UserPasswordChangeRequestDto dto
    ) {
        userService.changePassword(user.getEmail(), dto);
        return ResponseEntity.ok().build();
    }

    // 회원 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody @Valid UserDeleteRequestDto dto
    ) {
        userService.deleteUser(user.getEmail(), dto);
        return ResponseEntity.ok().build();
    }
}