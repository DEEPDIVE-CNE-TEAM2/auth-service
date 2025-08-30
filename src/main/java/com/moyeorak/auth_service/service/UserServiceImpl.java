package com.moyeorak.auth_service.service;

import com.moyeorak.auth_service.dto.*;
import com.moyeorak.auth_service.dto.feign.UserDto;
import com.moyeorak.auth_service.entity.User;
import com.moyeorak.common.exception.BusinessException;
import com.moyeorak.common.exception.ErrorCode;
import com.moyeorak.auth_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Transactional
    public UserSignupResponseDto signup(UserSignupRequestDto dto) {
        // 입력값 전처리
        String email = dto.getEmail().trim().toLowerCase();
        String phone = dto.getPhone().trim();

        // 비밀번호 확인 검증
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            log.debug("회원가입 실패 - 비밀번호 확인 불일치. email: {}", email);
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
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

    // 내 정보 조회
    @Override
    public UserResponseDto getMyInfo(String email) {
        User user = getUserByEmail(email);
        return UserResponseDto.fromEntity(user);
    }

    // 정보 수정
    @Transactional
    @Override
    public UserResponseDto updateUserInfo(String email, UserUpdateRequestDto dto) {
        User user = getUserByEmail(email);

        updateIfChanged(dto.getEmail(), user.getEmail(), newEmail -> {
            validateDuplicateEmail(newEmail);
            log.debug("이메일 변경 - {} -> {}", user.getEmail(), newEmail);
            user.setEmail(newEmail.trim().toLowerCase());
        });

        updateIfChanged(dto.getPhone(), user.getPhone(), newPhone -> {
            validateDuplicatePhone(newPhone);
            log.debug("전화번호 변경 - {} -> {}", user.getPhone(), newPhone);
            user.setPhone(newPhone.trim());
        });

        updateIfChanged(dto.getName(), user.getName(), user::setName);

        if (dto.getGender() != null && !dto.getGender().equals(user.getGender())) {
            log.debug("성별 변경 - {} -> {}", user.getGender(), dto.getGender());
            user.setGender(dto.getGender());
        }

        updateIfChanged(dto.getRegionId(), user.getRegionId(), user::setRegionId);

        return UserResponseDto.fromEntity(user);
    }

    // 비번 변경
    @Transactional
    @Override
    public void changePassword(String email, UserPasswordChangeRequestDto dto) {
        User user = getUserByEmail(email);

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            log.debug("비밀번호 변경 실패 - 현재 비밀번호 불일치. email: {}", email);
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        // 새 비밀번호 & 확인 비밀번호 검증
        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 동일 비밀번호 재사용 방지
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.SAME_PASSWORD);
        }

        // 새 비밀번호 저장
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    }

    // 회원탈퇴
    @Transactional
    @Override
    public void deleteUser(String email, UserDeleteRequestDto dto) {
        User user = getUserByEmail(email);

        // 비밀번호 & 확인 비밀번호 일치 여부 검증
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            log.debug("회원탈퇴 실패 - 비밀번호 확인 불일치. email: {}", email);
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        // 탈퇴 처리
        userRepository.delete(user);
    }


    // 이메일 중복 여부
    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email.trim().toLowerCase())) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
        }
    }

    // 번호 중복 여부
    private void validateDuplicatePhone(String phone) {
        if (userRepository.existsByPhone(phone.trim())) {
            throw new BusinessException(ErrorCode.PHONE_DUPLICATED);
        }
    }

    // 사용자 단건 조회 유틸
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
    }

    // 값 변경 감지 유틸
    private <T> void updateIfChanged(T newValue, T currentValue, Consumer<T> updater) {
        if (newValue != null && !newValue.equals(currentValue)) {
            updater.accept(newValue);
        }
    }

    // 이메일 중복 확인
    @Override
    public boolean isEmailDuplicate(String email) {
        return userRepository.existsByEmail(email.trim().toLowerCase());
    }

    // 휴대폰 번호 중복 확인
    @Override
    public boolean isPhoneDuplicate(String phone) {
        return userRepository.existsByPhone(phone.trim());
    }

    // 비밀번호 검증
    @Override
    public boolean verifyPassword(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
        return passwordEncoder.matches(password, user.getPassword());
    }


    public UserDto getUserDtoById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .regionId(user.getRegionId())
                .build();
    }
}
