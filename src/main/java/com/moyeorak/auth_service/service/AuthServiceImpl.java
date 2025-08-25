package com.moyeorak.auth_service.service;

import com.moyeorak.auth_service.dto.UserLoginRequestDto;
import com.moyeorak.auth_service.dto.UserLoginResponseDto;
import com.moyeorak.auth_service.entity.User;
import com.moyeorak.auth_service.security.JwtProvider;
import com.moyeorak.auth_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public UserLoginResponseDto login(UserLoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtProvider.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return new UserLoginResponseDto("로그인 완료", "Bearer " + accessToken, refreshToken);
    }

}
