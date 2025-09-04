package com.moyeorak.auth_service.security;

import org.springframework.beans.factory.annotation.Value;
import java.nio.charset.StandardCharsets;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

@Slf4j
@Component
public class JwtProvider {
    @Value("${jwt.private-key-path}")
    private String privateKeyPath;

    @Value("${jwt.public-key-path}")
    private String publicKeyPath;

    private PrivateKey privateKey;

    //RSA 개인키 로딩
    @PostConstruct
    public void init() {
        try {
            //ClassPathResource resource = new ClassPathResource("keys/private.pem");
            //String key = Files.readString(resource.getFile().toPath());
	    //String key = Files.readString(Paths.get(privateKeyPath), StandardCharsets.UTF_8);
		
            //key = key
	    InputStream is;
        	if (privateKeyPath != null && !privateKeyPath.isEmpty()) {
            	    // 환경변수 경로가 있으면 파일 시스템에서 로딩
            	    is = new FileInputStream(privateKeyPath);
        	} else {
            	    // 없으면 classpath에서 로딩 (테스트용)
            	    is = new ClassPathResource("keys/private.pem").getInputStream();
        	}
            String key = new String(is.readAllBytes(), StandardCharsets.UTF_8)
                    .replaceAll("-----BEGIN (.*)-----", "")
                    .replaceAll("-----END (.*)-----", "")
                    .replaceAll("\\R", ""); // 줄바꿈 제거

            byte[] keyBytes = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory factory = KeyFactory.getInstance("RSA");

            privateKey = factory.generatePrivate(spec);
            log.info("RSA PrivateKey 로딩 완료");

        } catch (IOException e) {
            log.error("private.pem 파일 읽기 실패: {}", e.getMessage());
        } catch (Exception e) {
            log.error("RSA PrivateKey 파싱 실패: {}", e.getMessage());
        }
    }

    // 액세스 토큰 생성
    public String generateToken(String email, Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roles", role);

        return createToken(email, claims, 1000L * 60 * 30); // 30분
    }

    //리프레시 토큰 생성
    public String generateRefreshToken(String email, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);

        return createToken(email, claims, 1000L * 60 * 60 * 24 * 14); // 14일
    }

    // 토큰 생성 공통 함수
    private String createToken(String subject, Map<String, Object> claims, long expiryMillis) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryMillis);

        return Jwts.builder()
                .setSubject(subject)
                .setClaims(claims)
                .setIssuer("https://api.moyeorak.cloud/auth")
                .setAudience("https://api.moyeorak.cloud")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    // 헤더에서 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        return (bearer != null && bearer.startsWith("Bearer ")) ? bearer.substring(7) : null;
    }



    // 이 아래로 테스트용 완성되면 지울 예정
    private PublicKey publicKey;

    @PostConstruct
    public void loadPublicKey() {
        try {
            //ClassPathResource resource = new ClassPathResource("keys/public.pem");
            //String key = Files.readString(resource.getFile().toPath());
	    //String key = Files.readString(Paths.get(publicKeyPath), StandardCharsets.UTF_8);

            //key = key
	    InputStream is;
                if (publicKeyPath != null && !publicKeyPath.isEmpty()) {
                    // 환경변수 경로가 있으면 파일 시스템에서 로딩
                    is = new FileInputStream(publicKeyPath);
                } else {
                    // 없으면 classpath에서 로딩 (테스트용)
                    is = new ClassPathResource("keys/public.pem").getInputStream();
                }
            String key = new String(is.readAllBytes(), StandardCharsets.UTF_8)
                    .replaceAll("-----BEGIN (.*)-----", "")
                    .replaceAll("-----END (.*)-----", "")
                    .replaceAll("\\R", "");

            byte[] keyBytes = Base64.getDecoder().decode(key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory factory = KeyFactory.getInstance("RSA");

            publicKey = factory.generatePublic(spec);
            log.info("RSA PublicKey loaded successfully");

        } catch (Exception e) {
            log.error("Failed to load public.pem", e);
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public String getRole(String token) {
        return parseClaims(token).get("roles", String.class);
    }

    public Long getUserId(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }


}
