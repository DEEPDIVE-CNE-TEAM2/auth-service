package com.moyeorak.auth_service.controller;

import com.moyeorak.auth_service.dto.UserLoginRequestDto;
import com.moyeorak.auth_service.dto.UserLoginResponseDto;
import com.moyeorak.auth_service.dto.UserSignupRequestDto;
import com.moyeorak.auth_service.dto.UserSignupResponseDto;
import com.moyeorak.auth_service.service.AuthService;
import com.moyeorak.auth_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}