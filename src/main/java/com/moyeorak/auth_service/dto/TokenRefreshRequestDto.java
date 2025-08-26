package com.moyeorak.auth_service.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TokenRefreshRequestDto {

    @NotBlank(message = "Refresh Token은 필수값입니다.")
    private String refreshToken;
}