package com.moyeorak.auth_service.service;

import com.moyeorak.auth_service.dto.UserSignupRequestDto;
import com.moyeorak.auth_service.dto.UserSignupResponseDto;

public interface UserService {

    UserSignupResponseDto signup(UserSignupRequestDto dto);



}