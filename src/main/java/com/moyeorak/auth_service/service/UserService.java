package com.moyeorak.auth_service.service;

import com.moyeorak.auth_service.dto.*;
import com.moyeorak.auth_service.dto.feign.UserDto;

public interface UserService {

    // 회원가입
    UserSignupResponseDto signup(UserSignupRequestDto dto);

    // 내 정보 조회 (userId 기반)
    UserResponseDto getMyInfo(Long userId);

    // 정보 수정 (userId 기반)
    UserResponseDto updateUserInfo(Long userId, UserUpdateRequestDto dto);

    // 비밀번호 변경 (userId 기반)
    void changePassword(Long userId, UserPasswordChangeRequestDto dto);

    // 회원탈퇴 (userId 기반)
    void deleteUser(Long userId, UserDeleteRequestDto dto);

    // 중복체크
    boolean isEmailDuplicate(String email);
    boolean isPhoneDuplicate(String phone);

    // 비밀번호 검증 (userId 기반)
    boolean verifyPassword(Long userId, String password);

    // Feign 통신용 DTO 반환 (id 기반)
    UserDto getUserDtoById(Long id);
}