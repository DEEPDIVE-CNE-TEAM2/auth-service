package com.moyeorak.auth_service.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class TokenResponseDto {
    private final String accessToken;
    private final String refreshToken;

    public static TokenResponseDto of(String accessToken, String refreshToken) {
        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
