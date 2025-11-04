package com.shrona.mommytalk.kakao.infrastructure.repository.query;

import com.shrona.mommytalk.kakao.domain.ChannelKakaoUser;
import com.shrona.mommytalk.kakao.domain.KakaoUser;
import java.util.List;

public interface KakaoQueryRepository {

    /**
     * userids에 해당하는 카카오 유저를 갖고 온다.
     */
    List<ChannelKakaoUser> findChannelKakaoUserByUserList(List<Long> userIds);

    /**
     * 카카오 아이디를 기준으로 카카오 정보를 갖고 온다.
     */
    KakaoUser findKakaoUserById(String id);

    /**
     * 휴대전화를 기준으로 카카오 아이디를 갖고 온다.
     */
    KakaoUser findKakaoUserByPhoneNumber(String phoneNumber);


}
