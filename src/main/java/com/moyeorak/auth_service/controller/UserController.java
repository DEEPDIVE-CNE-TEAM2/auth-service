package com.moyeorak.auth_service.controller;

import com.moyeorak.auth_service.dto.*;
import com.moyeorak.auth_service.dto.feign.UserDto;
import com.moyeorak.auth_service.entity.User;
import com.moyeorak.auth_service.repository.UserRepository;
import com.moyeorak.auth_service.service.AuthService;
import com.moyeorak.auth_service.service.UserService;
import com.moyeorak.common.exception.BusinessException;
import com.moyeorak.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import com.moyeorak.auth_service.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;


    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<UserSignupResponseDto> signup(@Valid @RequestBody UserSignupRequestDto dto) {
        log.info("회원가입 요청 진입 - email: {}", dto.getEmail());
        UserSignupResponseDto response = userService.signup(dto);
        log.info("회원가입 완료 - email: {}", response.getEmail());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyInfo(@AuthenticationPrincipal CustomUserDetails user) {
        log.info("내 정보 조회 요청 - email: {}", user.getEmail());
        UserResponseDto response = userService.getMyInfo(user.getEmail());
        log.info("내 정보 조회 완료 - email: {}", user.getEmail());
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "내 정보 수정")
    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateMyInfo(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody @Valid UserUpdateRequestDto dto
    ) {
        log.info("내 정보 수정 요청 - email: {}", user.getEmail());
        UserResponseDto response = userService.updateUserInfo(user.getEmail(), dto);
        log.info("내 정보 수정 완료 - email: {}", user.getEmail());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "비밀번호 변경")
    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody @Valid UserPasswordChangeRequestDto dto
    ) {
        log.info("비밀번호 변경 요청 - email: {}", user.getEmail());
        userService.changePassword(user.getEmail(), dto);
        log.info("비밀번호 변경 완료 - email: {}", user.getEmail());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원탈퇴")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody @Valid UserDeleteRequestDto dto
    ) {
        log.info("회원탈퇴 요청 - email: {}", user.getEmail());
        userService.deleteUser(user.getEmail(), dto);
        log.info("회원탈퇴 완료 - email: {}", user.getEmail());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "이메일 중복 확인")
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmailDuplicate(@RequestParam String email) {
        log.info("이메일 중복 확인 요청 - email: {}", email);
        boolean isDuplicate = userService.isEmailDuplicate(email);
        return ResponseEntity.ok(Map.of("isDuplicate", isDuplicate));
    }

    @Operation(summary = "번호 중복 확인")
    @GetMapping("/check-phone")
    public ResponseEntity<Map<String, Boolean>> checkPhoneDuplicate(@RequestParam String phone) {
        log.info("전화번호 중복 확인 요청 - phone: {}", phone);
        boolean isDuplicate = userService.isPhoneDuplicate(phone);
        return ResponseEntity.ok(Map.of("isDuplicate", isDuplicate));
    }

    @Operation(summary = "비밀번호 검증")
    @PostMapping("/verify-password")
    public ResponseEntity<PasswordVerifyResponseDto> verifyPassword(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody @Valid PasswordVerifyRequestDto dto
    ) {
        log.info("비밀번호 검증 요청 - email: {}", user.getEmail());
        boolean matched = userService.verifyPassword(user.getEmail(), dto.getPassword());
        log.info("비밀번호 검증 완료 - email: {}, matched: {}", user.getEmail(), matched);
        return ResponseEntity.ok(new PasswordVerifyResponseDto(matched));
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .regionId(user.getRegionId())
                .build();
    }
}