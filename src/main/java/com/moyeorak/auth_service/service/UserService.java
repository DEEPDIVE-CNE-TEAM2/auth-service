package com.moyeorak.auth_service.service;

import com.moyeorak.auth_service.dto.UserResponseDto;
import com.moyeorak.auth_service.dto.UserSignupRequestDto;
import com.moyeorak.auth_service.dto.UserSignupResponseDto;
import com.moyeorak.auth_service.dto.UserUpdateRequestDto;

public interface UserService {

    UserSignupResponseDto signup(UserSignupRequestDto dto);

    UserResponseDto getMyInfo(String email);

    UserResponseDto updateUserInfo(String email, UserUpdateRequestDto dto);

}