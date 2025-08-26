package com.moyeorak.auth_service.dto;

import com.moyeorak.auth_service.entity.User;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSignupRequestDto {

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    private String confirmPassword;

    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-0000-0000 형식이어야 합니다.")
    private String phone;

    @NotNull(message = "성별은 필수입니다.")
    private User.Gender gender;

    @NotNull(message = "생년월일은 필수입니다.")
    private LocalDate birth;

    private User.Role role;

    private Long regionId;

    // roll 이 널이면 기본값 설정 이건 서비스단으로 옮겨야함
    public User.Role getRoleOrDefault() {
        return role != null ? role : User.Role.USER;
    }
}
