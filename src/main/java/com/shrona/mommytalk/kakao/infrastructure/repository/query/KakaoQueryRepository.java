package com.shrona.mommytalk.kakao.infrastructure.repository.query;

import com.shrona.mommytalk.kakao.domain.ChannelKakaoUser;
import java.util.List;

public interface KakaoQueryRepository {

    /**
     * userids에 해당하는 카카오 유저를 갖고 온다.
     */
    List<ChannelKakaoUser> findChannelKakaoUserByUserList(List<Long> userIds);


}
