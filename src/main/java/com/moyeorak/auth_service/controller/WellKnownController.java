package com.moyeorak.auth_service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth/.well-known") // 최종 URL: /auth/.well-known/...
public class WellKnownController {

    private final RSAPublicKey publicKey;

    @Value("${jwt.public-key-path:}") // application.yml에서 주입, 없으면 빈 문자열
    private String publicKeyPath;

    public WellKnownController(@Value("${jwt.public-key-path:}") String publicKeyPath) throws Exception {
        InputStream is;
        if (publicKeyPath != null && !publicKeyPath.isEmpty()) {
            // 환경변수 또는 설정된 경로가 있으면 파일 시스템에서 로딩
            is = new FileInputStream(publicKeyPath);
        } else {
            // 없으면 classpath에서 로딩 (테스트용)
            is = new ClassPathResource("keys/public.pem").getInputStream();
        }

        String pem = new String(is.readAllBytes(), StandardCharsets.UTF_8)
                .replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)-----", "")
                .replaceAll("\\s", "");
        byte[] der = Base64.getDecoder().decode(pem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey pk = kf.generatePublic(spec);
        this.publicKey = (RSAPublicKey) pk;
    }

    @GetMapping("/openid-configuration")
    public Map<String, Object> openidConfiguration() {
        // 반드시 iss와 정확히 일치해야 함
        String issuer = "https://api.moyeorak.cloud/auth";
        String jwksUri = issuer + "/.well-known/jwks.json";

        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("issuer", issuer);
        doc.put("jwks_uri", jwksUri);
        // 최소 필드만 넣어도 되지만 아래 값들은 관례적으로 포함
        doc.put("id_token_signing_alg_values_supported", List.of("RS256"));
        doc.put("response_types_supported", List.of("token"));
        doc.put("subject_types_supported", List.of("public"));
        return doc;
    }

    @GetMapping("/jwks.json")
    public Map<String, Object> jwks() {
        // RSAPublicKey → JWK (n, e는 "부호 없는" base64url 인코딩)
        String n = base64UrlUnsigned(publicKey.getModulus());
        String e = base64UrlUnsigned(publicKey.getPublicExponent());

        Map<String, Object> jwk = new LinkedHashMap<>();
        jwk.put("kty", "RSA");
        jwk.put("alg", "RS256");
        jwk.put("use", "sig");
        jwk.put("kid", "moyeorak-key-1"); // ← 토큰 header의 kid와 반드시 동일
        jwk.put("n", n);
        jwk.put("e", e);

        return Map.of("keys", List.of(jwk));
    }

    private static String base64UrlUnsigned(BigInteger i) {
        // BigInteger.toByteArray()는 선행 0x00(부호 바이트)이 붙을 수 있어 제거 필요
        byte[] bytes = i.toByteArray();
        if (bytes.length > 1 && bytes[0] == 0x00) {
            byte[] tmp = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, tmp, 0, tmp.length);
            bytes = tmp;
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
