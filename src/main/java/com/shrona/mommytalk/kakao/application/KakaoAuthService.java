package com.shrona.mommytalk.kakao.application;

import static com.shrona.mommytalk.channel.common.exception.ChannelErrorCode.CHANNEL_NOT_FOUND;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.shrona.mommytalk.auth.presentation.dtos.response.UserAuthResponseDto;
import com.shrona.mommytalk.channel.application.ChannelService;
import com.shrona.mommytalk.channel.common.exception.ChannelException;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.common.utils.JwtUtils;
import com.shrona.mommytalk.kakao.domain.ChannelKakaoUser;
import com.shrona.mommytalk.kakao.domain.KakaoUser;
import com.shrona.mommytalk.kakao.infrastructure.repository.jpa.KakaoUserJpaRepository;
import com.shrona.mommytalk.kakao.infrastructure.repository.query.KakaoQueryRepository;
import com.shrona.mommytalk.kakao.infrastructure.sender.KakaoAuthClient;
import com.shrona.mommytalk.kakao.infrastructure.sender.dto.KakaoTokenResponse;
import com.shrona.mommytalk.kakao.infrastructure.sender.dto.KakaoUserInfoResponse;
import com.shrona.mommytalk.user.application.UserService;
import com.shrona.mommytalk.user.common.exception.UserErrorCode;
import com.shrona.mommytalk.user.common.exception.UserException;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.UserRole;
import com.shrona.mommytalk.user.domain.type.OnBoardingStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional(readOnly = true)
public class KakaoAuthService {

    private final KakaoService kakaoService;
    private final UserService userService;
    private final ChannelService channelService;

    private final RestClient kakaoAuthRestClient;
    private final KakaoAuthClient kakaoAuthClient;

    private final KakaoQueryRepository kakaoQueryRepository;
    private final KakaoUserJpaRepository kakaoUserJpaRepository;

    private final JwtUtils jwtUtils;

    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    @Value("${kakao.oauth.client-id}")
    private String clientId;

    @Value("${kakao.oauth.client-secret}")
    private String clientSecret;

    /**
     * Kakao OAuth 콜백 처리
     */
    @Transactional
    public UserAuthResponseDto processCallback(
        String code, String channelCode, String redirectUri) {

        // TODO: 채널을 어떻게 좋게 넣을 수 있을까
        Channel channel = channelService.findChannelByCode(channelCode)
            .orElseThrow(() -> new ChannelException(CHANNEL_NOT_FOUND));

        // Authorization Code로 Access Token 교환
        KakaoTokenResponse tokenResponse = exchangeCodeForToken(code, redirectUri);

        // Access Token으로 사용자 정보 조회 (기본 API - 휴대전화 포함)
        KakaoUserInfoResponse userInfo = getUserInfo(tokenResponse.accessToken());

        KakaoUser kakaoUserInfo = checkExistUser(userInfo);

        try {
            // 카카오 유저가 없으면 새로 생성해준다
            if (kakaoUserInfo == null) {

                List<User> userList = userService.findOrCreateUsersByPhoneNumbers(
                    List.of(convertPhoneNumber(userInfo.getPhoneNumber())));

                ChannelKakaoUser channelKakaoUser = kakaoService.upsertChannelKakaoUser(
                    channel,
                    userList.getFirst(),
                    userInfo.id().toString());

                kakaoUserInfo = channelKakaoUser.getKakaoUser();
            }
        } catch (Exception e) {
            log.error("카카오 유저 생성 중 오류 발생\n카카오 유저 아이디 : {}, 카카오 아이디 : {}, 휴대 전화: {}, 변환 번호 : {}",
                userInfo.id(), userInfo.getKakaoId(), userInfo.getPhoneNumber(),
                convertPhoneNumber(userInfo.getPhoneNumber()));
            throw new UserException(UserErrorCode.INTERNAL_SERVER_EXCEPTION);
        }

        // 로그인 시간 업데이트
        kakaoUserInfo.getUser().updateLastLoginDate();

        String token = jwtUtils.createToken(kakaoUserInfo.getUser().getId(), UserRole.USER);

        // 3. 유저 조회 및 생성 로직은 호출하는 쪽에서 처리
        return UserAuthResponseDto.of(
            token,
            kakaoUserInfo.getUser().getId(),
            2L,
            kakaoUserInfo.getUser().getName(),
            kakaoUserInfo.getUser().getOnboardingStatus().equals(OnBoardingStatus.TRUE));
    }

    /**
     * Authorization Code를 Access Token으로 교환
     */
    private KakaoTokenResponse exchangeCodeForToken(String code, String redirectUri) {
        try {
            // Form data 생성
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "authorization_code");
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);
            formData.add("redirect_uri", redirectUri);
            formData.add("code", code);

            return kakaoAuthRestClient.post()
                .uri("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(KakaoTokenResponse.class);

        } catch (HttpClientErrorException e) {
            log.error("카카오 토큰 교환 실패: {}", e.getResponseBodyAsString());
            throw new RuntimeException("카카오 토큰 교환 실패", e);
        }
    }

    /**
     * Access Token으로 사용자 정보 조회 (/v2/user/me)
     * 휴대전화 번호 포함
     */
    private KakaoUserInfoResponse getUserInfo(String accessToken) {
        try {
            String authorization = "Bearer " + accessToken;
            return kakaoAuthClient.getUserInfo(authorization);

        } catch (HttpClientErrorException e) {
            log.error("카카오 사용자 정보 조회 실패: {}", e.getResponseBodyAsString());
            throw new RuntimeException("카카오 사용자 정보 조회 실패", e);
        }
    }

    /**
     * 카카오에서 넘어오는 휴대전화 번호를 변환
     * 국제 형식(+82, +1)을 감지하여 자동으로 국내 형식으로 변환
     */
    private String convertPhoneNumber(String kakaoFormatPhone) {
        try {
            // Google PhoneNumberUtil로 파싱 (국가 코드 자동 감지)
            PhoneNumber number = phoneUtil.parse(kakaoFormatPhone, null);
            int countryCode = number.getCountryCode();

            switch (countryCode) {
                case 82:  // 한국 (+82)
                    return phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);

                case 1:   // 미국/캐나다 (+1)
                    // (650) 123-4567 → 1-650-123-4567 변환
                    String nationalFormat = phoneUtil.format(number,
                        PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
                    String digitsOnly = nationalFormat.replaceAll("[^0-9]", "");

                    if (digitsOnly.length() == 10) {
                        return "1-" + digitsOnly.substring(0, 3) + "-" +
                            digitsOnly.substring(3, 6) + "-" +
                            digitsOnly.substring(6);
                    }

                    log.warn("미국 전화번호 형식 오류: {}", nationalFormat);
                    throw new UserException(UserErrorCode.INVALID_PHONE_NUMBER_INPUT);

                default:
                    log.error("지원하지 않는 국가 코드: {}, 전화번호: {}", countryCode, kakaoFormatPhone);
                    throw new UserException(UserErrorCode.INVALID_PHONE_NUMBER_INPUT);
            }

        } catch (NumberParseException exception) {
            log.error("휴대전화 포맷 파싱 실패: {}", kakaoFormatPhone, exception);
            throw new UserException(UserErrorCode.INVALID_PHONE_NUMBER_INPUT);
        }
    }

    /**
     * 현재 카카오 유저 아이디가 존재하는 지 확인한다.
     */
    @Transactional
    private KakaoUser checkExistUser(KakaoUserInfoResponse infoResponse) {
        // 카카오 id가 존재 하지 않는다면 휴대전화로 조회를 하고 id를 업데이트 해준다.
        KakaoUser kakaoUserById = kakaoQueryRepository.findKakaoUserById(
            infoResponse.id().toString());
        if (kakaoUserById == null) {
            KakaoUser kakaoUserByPhoneNumber = kakaoQueryRepository.findKakaoUserByPhoneNumber(
                convertPhoneNumber(infoResponse.getPhoneNumber()));

            // 현재 휴대폰 유저가 존재하고 해당 카카오에 유저 아이디가 존재하지 않으면 업데이트를 진행한다.
            if (kakaoUserByPhoneNumber != null) {
                kakaoUserByPhoneNumber.setFirstKakaoId(infoResponse.id().toString());
                return kakaoUserJpaRepository.save(kakaoUserByPhoneNumber);
            } else { // 유저가 아예 없으면 null 반환
                return null;
            }
        }
        return kakaoUserById;
    }
}
