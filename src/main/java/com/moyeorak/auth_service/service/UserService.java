package com.moyeorak.auth_service.service;

import com.moyeorak.auth_service.dto.*;

public interface UserService {

    UserSignupResponseDto signup(UserSignupRequestDto dto);

    UserResponseDto getMyInfo(String email);

    UserResponseDto updateUserInfo(String email, UserUpdateRequestDto dto);
    void changePassword(String email, UserPasswordChangeRequestDto dto);
    void deleteUser(String email, UserDeleteRequestDto dto);

}