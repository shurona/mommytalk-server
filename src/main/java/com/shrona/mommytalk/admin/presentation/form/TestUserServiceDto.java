package com.shrona.mommytalk.admin.presentation.form;

import com.shrona.mommytalk.admin.domain.TestUser;
import com.shrona.mommytalk.channel.domain.ChannelPlatform;
import com.shrona.mommytalk.kakao.domain.KakaoUser;
import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import java.util.Optional;

public record TestUserServiceDto(
    Long id,
    String phoneNumber,
    String socialId
) {

    public static TestUserServiceDto of(TestUser testUser, ChannelPlatform channelPlatform) {
        String phoneNumber = Optional.ofNullable(testUser.getUser())
            .map(User::getPhoneNumber)
            .map(PhoneNumber::getPhoneNumber)
            .orElse(null);

        String socialId = null;
        switch (channelPlatform) {
            case KAKAO -> {
                socialId = Optional.ofNullable(testUser.getUser())
                    .map(User::getKakaoUser)
                    .map(KakaoUser::getKakaoId)
                    .orElse(null);

            }
            case LINE -> socialId = Optional.ofNullable(testUser.getUser())
                .map(User::getLineUser)
                .map(LineUser::getLineId)
                .orElse(null);
        }
        return new TestUserServiceDto(
            testUser.getId(),
            phoneNumber,
            socialId
        );
    }

}
