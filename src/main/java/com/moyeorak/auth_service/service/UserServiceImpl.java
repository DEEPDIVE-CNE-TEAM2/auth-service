package com.moyeorak.auth_service.service;

import com.moyeorak.auth_service.dto.UserSignupRequestDto;
import com.moyeorak.auth_service.dto.UserSignupResponseDto;
import com.moyeorak.auth_service.entity.User;
import com.moyeorak.auth_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserSignupResponseDto signup(UserSignupRequestDto dto) {
        // 입력값 전처리
        String email = dto.getEmail().trim().toLowerCase();
        String phone = dto.getPhone().trim();

        // 비밀번호 확인 검증
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        // 이메일 번호 중복 검증
        validateDuplicateEmail(email);
        validateDuplicatePhone(phone);

        // 유저 엔티티 생성
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .gender(dto.getGender())
                .phone(phone)
                .birth(dto.getBirth())
                .role(User.Role.USER) //기본값 USER
                .regionId(dto.getRegionId())
                .build();

        User savedUser = userRepository.save(user);

        return UserSignupResponseDto.builder()
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .phone(savedUser.getPhone())
                .regionId(savedUser.getRegionId())
                .build();
    }


    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email.trim().toLowerCase())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
    }

    private void validateDuplicatePhone(String phone) {
        if (userRepository.existsByPhone(phone.trim())) {
            throw new IllegalArgumentException("이미 존재하는 전화번호입니다.");
        }
    }


}
