package com.shrona.mommytalk.common.utils;

import static com.shrona.mommytalk.user.common.exception.UserErrorCode.JWT_TOKEN_INVALID;

import com.shrona.mommytalk.user.common.exception.UserException;
import com.shrona.mommytalk.user.domain.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class JwtUtils {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    // Jwt payload에서 사용자 권한 값의 KEY
    public static final String AUTHORIZATION_KEY = "auth";

    // Token 식별자
    public static final String BEARER_PREFIX = "Bearer ";

    // access Token 유효기간
    public static final long TOKEN_TIME = 24 * 60 * 30 * 1000L; // 30분

    // Refresh Token 유효기간
    public static final long REFRESH_TOKEN_TIME = 14 * 24 * 60 * 60 * 1000L;


    private String secretKey;
    private SecretKey key;

    public JwtUtils(@Value("${jwt.secret.key}") String secretKey) {
        this.secretKey = secretKey;
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    public String createToken(Long userId, UserRole role) {
        Date date = new Date();

        return
            Jwts.builder()
                .subject(String.valueOf(userId)) // 사용자 식별자값(ID)
                .claim(AUTHORIZATION_KEY, role.getAuthority()) // 사용자 권한
                .expiration(new Date(date.getTime() + TOKEN_TIME)) // 만료 시간
                .issuedAt(date) // 발급일
                .signWith(key) // 암호화
                .compact();
    }


    public String substringToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            return tokenValue.substring(7);
        }

        throw new UserException(JWT_TOKEN_INVALID);
    }

    /**
     * 토큰의 Validation 확인
     */
    public boolean checkValidJwtToken(String jwtToken) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(jwtToken);
            return true;
        } catch (Exception e) {
//            log.error("에러 원인 : {} 에러 설명 : {}", e.getCause(), e.getMessage());
        }
        return false;
    }

    /**
     * payload 정보 갖고 오기
     */
    public Claims getBodyFromJwt(String jwtToken) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(jwtToken).getPayload();
    }


    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(Long userId) {
        Date date = new Date();

        return BEARER_PREFIX +
            Jwts.builder()
                .subject(String.valueOf(userId))
                .expiration(new Date(date.getTime() + REFRESH_TOKEN_TIME))
                .issuedAt(date)
                .signWith(key)
                .compact();
    }

    /**
     * Refresh Token으로 새 Access Token 생성
     */
    public String refreshAccessToken(String refreshToken) {
        // 리프레시 토큰 유효성 검사
        if (!checkValidJwtToken(substringToken(refreshToken))) {
            throw new UserException(JWT_TOKEN_INVALID);
        }

        // 리프레시 토큰에서 사용자 ID 추출
        Claims claims = getBodyFromJwt(substringToken(refreshToken));
        Long userId = Long.parseLong(claims.getSubject());

        // DB에서 사용자 정보와 권한 조회 필요 (서비스 계층에서 구현)
        UserRole role = UserRole.USER; // 예시, 실제로는 DB에서 조회

        // 새 액세스 토큰 생성
        return createToken(userId, role);
    }

}
