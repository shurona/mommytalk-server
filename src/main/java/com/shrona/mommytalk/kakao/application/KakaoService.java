package com.shrona.mommytalk.kakao.application;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.user.domain.User;
import java.util.List;

public interface KakaoService {

    /**
     * 유저 목록에서 없는 유저를 카카오 채널에 추가해준다.
     */
    void addKakaoUserFromUserList(Channel channel, List<User> userList);


}
