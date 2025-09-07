package com.moyeorak.auth_service.controller;

import com.moyeorak.auth_service.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/jwks")
@RequiredArgsConstructor
public class TokenController {

    private final JwtProvider jwtProvider;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getJwks() {
        return ResponseEntity.ok(jwtProvider.getJwks());
    }
}