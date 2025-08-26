package com.moyeorak.auth_service.service;

import com.moyeorak.auth_service.dto.*;
import com.moyeorak.auth_service.entity.User;
import com.moyeorak.auth_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

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

    // ✅ 내 정보 조회
    @Override
    public UserResponseDto getMyInfo(String email) {
        User user = getUserByEmail(email);
        return UserResponseDto.fromEntity(user);
    }

    @Transactional
    @Override
    public UserResponseDto updateUserInfo(String email, UserUpdateRequestDto dto) {
        User user = getUserByEmail(email);

        updateIfChanged(dto.getEmail(), user.getEmail(), newEmail -> {
            validateDuplicateEmail(newEmail);
            user.setEmail(newEmail.trim().toLowerCase());
        });

        updateIfChanged(dto.getPhone(), user.getPhone(), newPhone -> {
            validateDuplicatePhone(newPhone);
            user.setPhone(newPhone.trim());
        });

        updateIfChanged(dto.getName(), user.getName(), user::setName);

        if (dto.getGender() != null && !dto.getGender().equals(user.getGender())) {
            user.setGender(dto.getGender());
        }

        updateIfChanged(dto.getRegionId(), user.getRegionId(), user::setRegionId);

        return UserResponseDto.fromEntity(user);
    }

    @Transactional
    @Override
    public void changePassword(String email, UserPasswordChangeRequestDto dto) {
        User user = getUserByEmail(email);

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 & 확인 비밀번호 검증
        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new IllegalArgumentException("새 비밀번호와 확인 값이 일치하지 않습니다.");
        }

        // 동일 비밀번호 재사용 방지
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이전과 동일한 비밀번호는 사용할 수 없습니다.");
        }

        // 새 비밀번호 저장
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    }

    @Transactional
    @Override
    public void deleteUser(String email, UserDeleteRequestDto dto) {
        User user = getUserByEmail(email);

        // 비밀번호 & 확인 비밀번호 일치 여부 검증
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 탈퇴 처리
        userRepository.delete(user);
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

    // 사용자 단건 조회 유틸
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    // 값 변경 감지 유틸
    private <T> void updateIfChanged(T newValue, T currentValue, Consumer<T> updater) {
        if (newValue != null && !newValue.equals(currentValue)) {
            updater.accept(newValue);
        }
    }


}
