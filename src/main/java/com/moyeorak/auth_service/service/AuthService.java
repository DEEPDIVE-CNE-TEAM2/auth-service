package com.moyeorak.auth_service.service;

import com.moyeorak.auth_service.dto.UserLoginRequestDto;
import com.moyeorak.auth_service.dto.UserLoginResponseDto;

public interface AuthService {

    UserLoginResponseDto login(UserLoginRequestDto dto);

}