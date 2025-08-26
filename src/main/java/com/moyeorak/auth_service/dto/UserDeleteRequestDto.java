package com.moyeorak.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDeleteRequestDto {

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String confirmPassword;
}
