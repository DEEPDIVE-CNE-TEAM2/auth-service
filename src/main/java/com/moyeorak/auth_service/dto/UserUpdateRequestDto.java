package com.moyeorak.auth_service.dto;

import com.moyeorak.auth_service.entity.User;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequestDto {
    private String email;
    private String phone;
    private String name;
    private User.Gender gender;
    private Long regionId;
}
