package com.moyeorak.auth_service.controller;

import com.moyeorak.auth_service.dto.*;
import com.moyeorak.auth_service.service.AuthService;
import com.moyeorak.auth_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.moyeorak.auth_service.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<UserSignupResponseDto> signup(@Valid @RequestBody UserSignupRequestDto dto) {
        return ResponseEntity.ok(userService.signup(dto));
    }

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyInfo(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(userService.getMyInfo(user.getEmail()));
    }

    @Operation(summary = "내 정보 수정")
    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateMyInfo(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody @Valid UserUpdateRequestDto dto
    ) {
        return ResponseEntity.ok(userService.updateUserInfo(user.getEmail(), dto));
    }

    @Operation(summary = "비밀번호 변경")
    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody @Valid UserPasswordChangeRequestDto dto
    ) {
        userService.changePassword(user.getEmail(), dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원가입")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody @Valid UserDeleteRequestDto dto
    ) {
        userService.deleteUser(user.getEmail(), dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "이메일 중복 확인")
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmailDuplicate(@RequestParam String email) {
        boolean isDuplicate = userService.isEmailDuplicate(email);
        return ResponseEntity.ok(Map.of("isDuplicate", isDuplicate));
    }

    @Operation(summary = "번호 중복 확인")
    @GetMapping("/check-phone")
    public ResponseEntity<Map<String, Boolean>> checkPhoneDuplicate(@RequestParam String phone) {
        boolean isDuplicate = userService.isPhoneDuplicate(phone);
        return ResponseEntity.ok(Map.of("isDuplicate", isDuplicate));
    }

    @Operation(summary = "비밀번호 검증")
    @PostMapping("/verify-password")
    public ResponseEntity<PasswordVerifyResponseDto> verifyPassword(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody @Valid PasswordVerifyRequestDto dto
    ) {
        boolean matched = userService.verifyPassword(user.getEmail(), dto.getPassword());
        return ResponseEntity.ok(new PasswordVerifyResponseDto(matched));
    }
}