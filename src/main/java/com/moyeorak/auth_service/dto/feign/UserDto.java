package com.moyeorak.auth_service.dto.feign;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String email;
    private Long regionId;
}