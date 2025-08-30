package com.moyeorak.auth_service.controller;

import com.moyeorak.auth_service.dto.feign.UserDto;
import com.moyeorak.auth_service.entity.User;
import com.moyeorak.auth_service.repository.UserRepository;
import com.moyeorak.auth_service.service.UserService;
import com.moyeorak.common.exception.BusinessException;
import com.moyeorak.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalController {

    private final UserService userService;

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        log.info("내부 사용자 조회 요청 - userId={}", id);
        return userService.getUserDtoById(id);
    }
}
