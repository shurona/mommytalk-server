package com.shrona.mommytalk.auth.application;

import static com.shrona.mommytalk.auth.common.exception.AuthErrorCode.PROFILE_RETRIEVAL_FAILED;
import static com.shrona.mommytalk.auth.common.exception.AuthErrorCode.TOKEN_EXCHANGE_FAIL;

import com.shrona.mommytalk.auth.common.exception.AuthCustomException;
import com.shrona.mommytalk.auth.presentation.dtos.response.LineAuthResponseDto;
import com.shrona.mommytalk.auth.presentation.dtos.response.LineTokenResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@Transactional
public class LineAuthService {

    private static final String LINE_TOKEN_URL = "https://api.line.me/oauth2/v2.1/token";
    private static final String LINE_PROFILE_URL = "https://api.line.me/v2/profile";
    //    @Autowired
//    private LineUserRepository lineUserRepository;
//    @Autowired
//    private AdminLineAccountRepository adminLineAccountRepository;
    @Value("${line.auth.channel-id}")
    private String channelId;
    @Value("${line.auth.secret-key}")
    private String channelSecret;

    /**
     * LINE OAuth 콜백 처리
     */
    public LineAuthResponseDto processCallback(String code, String state,
        String redirectUri) {
        // 1. Authorization Code로 Access Token 교환
        LineTokenResponse tokenResponse = exchangeCodeForToken(code, redirectUri);

        // 2. Access Token으로 사용자 프로필 조회
        LineProfile lineProfile = getUserProfile(tokenResponse.getAccessToken());

        // 3. LINE 사용자 정보 저장/업데이트
//        boolean isNewUser = false;
//        LineUser lineUser = lineUserRepository.findByLineUserId(lineProfile.getUserId())
//            .orElse(null);
//
//        if (lineUser == null) {
//            lineUser = LineUser.builder()
//                .lineUserId(lineProfile.getUserId())
//                .build();
//            isNewUser = true;
//        }
//
//        // 프로필 정보 업데이트
//        lineUser.setDisplayName(lineProfile.getDisplayName());
//        lineUser.setPictureUrl(lineProfile.getPictureUrl());
//        lineUser.setStatusMessage(lineProfile.getStatusMessage());
//
//        // 이메일 정보 조회 (별도 API 호출)
//        try {
//            String email = getUserEmail(tokenResponse.getAccessToken());
//            lineUser.setEmail(email);
//        } catch (Exception e) {
//            log.warn("이메일 정보 조회 실패: {}", e.getMessage());
//        }
//
//        lineUser = lineUserRepository.save(lineUser);
//
//        // 4. 관리자-LINE 계정 연결 저장/업데이트
//        AdminLineAccount linkAccount = adminLineAccountRepository
//            .findByAdminId(adminId)
//            .orElse(AdminLineAccount.builder()
//                .adminId(adminId)
//                .build());
//
//        linkAccount.setLineUserId(lineProfile.getUserId());
//        adminLineAccountRepository.save(linkAccount);

        return LineAuthResponseDto.of();
    }

    /**
     * Authorization Code를 Access Token으로 교환
     */
    private LineTokenResponse exchangeCodeForToken(String code, String redirectUri) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("redirect_uri", redirectUri);
        params.add("client_id", channelId);
        params.add("client_secret", channelSecret);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<LineTokenResponse> response = restTemplate.exchange(
                LINE_TOKEN_URL,
                HttpMethod.POST,
                entity,
                LineTokenResponse.class
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("토큰 교환 실패: {}", e.getResponseBodyAsString());
            throw new AuthCustomException(TOKEN_EXCHANGE_FAIL);
        }
    }

    /**
     * Access Token으로 사용자 프로필 조회
     */
    private LineProfile getUserProfile(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        try {
            ResponseEntity<LineProfile> response = restTemplate.exchange(
                LINE_PROFILE_URL,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                LineProfile.class
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("프로필 조회 실패: {}", e.getResponseBodyAsString());
            throw new AuthCustomException(PROFILE_RETRIEVAL_FAILED);
        }
    }

    /**
     * 이메일 정보 조회
     */
    private String getUserEmail(String accessToken) {
        // ID Token verify API 또는 별도 이메일 API 호출
        // 구현 방법은 LINE 개발자 문서 참조
        return null; // 예시에서는 null 반환
    }

    /**
     * 연결된 LINE 프로필 조회
     */
//    @Transactional(readOnly = true)
//    public LineProfileResponse getLinkedProfile(Long adminId) {
//        AdminLineAccount linkAccount = adminLineAccountRepository
//            .findByAdminId(adminId)
//            .orElseThrow(() -> new LinkedAccountNotFoundException("No linked account"));
//
//        LineUser lineUser = lineUserRepository
//            .findByLineUserId(linkAccount.getLineUserId())
//            .orElseThrow(() -> new LinkedAccountNotFoundException("LINE user not found"));
//
//        return LineProfileResponse.builder()
//            .lineUser(lineUser)
//            .linkedAt(linkAccount.getLinkedAt())
//            .build();
//    }
//
//    /**
//     * LINE 계정 연결 해제
//     */
//    public LineUnlinkResponse unlinkAccount(Long adminId) {
//        AdminLineAccount linkAccount = adminLineAccountRepository
//            .findByAdminId(adminId)
//            .orElseThrow(() -> new LinkedAccountNotFoundException("No linked account"));
//
//        adminLineAccountRepository.delete(linkAccount);
//
//        return LineUnlinkResponse.builder()
//            .unlinkedAt(LocalDateTime.now())
//            .build();
//    }
}