package com.moyeorak.auth_service.service;

import com.moyeorak.auth_service.dto.TokenResponseDto;
import com.moyeorak.auth_service.dto.UserLoginRequestDto;
import com.moyeorak.auth_service.dto.UserLoginResponseDto;
import com.moyeorak.auth_service.entity.User;
import com.moyeorak.common.exception.BusinessException;
import com.moyeorak.common.exception.ErrorCode;
import com.moyeorak.auth_service.security.JwtProvider;
import com.moyeorak.auth_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public UserLoginResponseDto login(UserLoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.debug("로그인 실패 - 비밀번호 불일치: {}", dto.getEmail());
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtProvider.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return new UserLoginResponseDto("로그인 완료", "Bearer " + accessToken, refreshToken);
    }

    @Override
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        user.setRefreshToken(null);
        userRepository.save(user);
    }

    @Override
    public TokenResponseDto refreshAccessToken(String refreshToken) {
        // Refresh Token 유효성 검증 후 이메일 추출
        if (!jwtProvider.validateToken(refreshToken)) {
            log.debug("Refresh Token 유효성 실패");
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        String email = jwtProvider.getEmail(refreshToken);

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        // DB에 저장된 Refresh Token과 일치 여부 확인
        if (!refreshToken.equals(user.getRefreshToken())) {
            log.debug("DB 저장된 Refresh Token 불일치 - email: {}", email);
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 새 Access Token / Refresh Token 발급
        String newAccessToken = jwtProvider.generateToken(email, user.getRole().name());
        String newRefreshToken = jwtProvider.generateRefreshToken(email);

        // Refresh Token 갱신
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return new TokenResponseDto("Bearer " + newAccessToken, newRefreshToken);
    }
}
