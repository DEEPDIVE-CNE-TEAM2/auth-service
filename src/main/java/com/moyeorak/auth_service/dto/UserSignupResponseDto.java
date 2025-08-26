package com.moyeorak.auth_service.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSignupResponseDto {
    private String email;
    private String name;
    private String phone;
    private Long regionId;
}