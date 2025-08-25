package com.moyeorak.auth_service.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequestDto {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}