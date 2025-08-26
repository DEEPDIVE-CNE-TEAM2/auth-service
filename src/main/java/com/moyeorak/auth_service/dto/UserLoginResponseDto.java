package com.moyeorak.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserLoginResponseDto {
    private String message;
    private String accessToken;
    private String refreshToken;
}