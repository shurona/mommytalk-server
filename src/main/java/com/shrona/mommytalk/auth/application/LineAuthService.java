package com.shrona.mommytalk.auth.application;

import static com.shrona.mommytalk.auth.common.exception.AuthErrorCode.PROFILE_RETRIEVAL_FAILED;
import static com.shrona.mommytalk.auth.common.exception.AuthErrorCode.TOKEN_EXCHANGE_FAIL;
import static com.shrona.mommytalk.channel.common.exception.ChannelErrorCode.CHANNEL_NOT_FOUND;

import com.shrona.mommytalk.auth.common.exception.AuthCustomException;
import com.shrona.mommytalk.auth.infrastructure.sender.LineAuthClient;
import com.shrona.mommytalk.auth.infrastructure.sender.dto.LineProfileResponse;
import com.shrona.mommytalk.auth.presentation.dtos.response.LineAuthResponseDto;
import com.shrona.mommytalk.auth.presentation.dtos.response.LineTokenResponse;
import com.shrona.mommytalk.channel.application.ChannelService;
import com.shrona.mommytalk.channel.common.exception.ChannelException;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.common.utils.JwtUtils;
import com.shrona.mommytalk.line.application.LineService;
import com.shrona.mommytalk.line.domain.ChannelLineUser;
import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.UserRole;
import com.shrona.mommytalk.user.infrastructure.repository.jpa.UserJpaRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class LineAuthService {

    // restClient
    private final RestClient lineAuthRestClient;
    private final LineAuthClient lineAuthClient;

    // service
    private final LineService lineService;
    private final ChannelService channelService;

    // repository
    private final UserJpaRepository userRepository;

    // jwt Utils
    private final JwtUtils jwtUtils;

    @Value("${line.auth.channel-id}")
    private String channelId;
    @Value("${line.auth.secret-key}")
    private String channelSecret;

    /**
     * LINE OAuth 콜백 처리
     */
    public LineAuthResponseDto processCallback(
        String code, String state, String redirectUri) {
        // 1. Authorization Code로 Access Token 교환
        LineTokenResponse tokenResponse = exchangeCodeForToken(code, redirectUri);

        // 2. Access Token으로 사용자 프로필 조회
        LineProfileResponse lineProfile = getUserProfile(tokenResponse.getAccessToken());

        Channel channel = channelService.findChannelById(1L)
            .orElseThrow(() -> new ChannelException(CHANNEL_NOT_FOUND));

        // socialId 정보를 갖고 온다. 없으면 저장해준다.
        LineUser lineUserInfo = lineService.findOrCreateLineUser(lineProfile.userId());
        ChannelLineUser channelLineUser = lineService.findOrChannelLineUser(channel,
            lineUserInfo);

        Optional<User> userInfo = userRepository.findByLineUser(lineUserInfo);

        // orElseGet을 사용하여 유저가 없을 때만 새로운 유저 생성 및 저장
        User user = userInfo.orElseGet(() -> {
            User newUser = User.createUserWithLine(null, lineUserInfo);
            return userRepository.save(newUser);
        });

        String token = jwtUtils.createToken(user.getId(), UserRole.USER);

        return LineAuthResponseDto.of(token, user.getId(), user.getName(), false);
    }

    /**
     * Authorization Code를 Access Token으로 교환
     */
    private LineTokenResponse exchangeCodeForToken(String code, String redirectUri) {
        try {
            // Form data 생성
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "authorization_code");
            formData.add("code", code);
            formData.add("redirect_uri", redirectUri);
            formData.add("client_id", channelId);
            formData.add("client_secret", channelSecret);

            return lineAuthRestClient.post()
                .uri("/oauth2/v2.1/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(LineTokenResponse.class);

        } catch (HttpClientErrorException e) {
            log.error("토큰 교환 실패: {}", e.getResponseBodyAsString());
            throw new AuthCustomException(TOKEN_EXCHANGE_FAIL);
        }
    }

    /**
     * Access Token으로 사용자 프로필 조회
     */
    private LineProfileResponse getUserProfile(String accessToken) {
        try {
            String authorization = "Bearer " + accessToken;
            return lineAuthClient.getUserProfile(authorization);

        } catch (HttpClientErrorException e) {
            log.error("프로필 조회 실패: {}", e.getResponseBodyAsString());
            throw new AuthCustomException(PROFILE_RETRIEVAL_FAILED);
        }
    }
}