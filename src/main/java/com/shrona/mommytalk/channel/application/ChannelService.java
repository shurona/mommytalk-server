package com.shrona.mommytalk.channel.application;

import com.shrona.mommytalk.channel.domain.Channel;
import java.util.List;
import java.util.Optional;

public interface ChannelService {

    /**
     * 채널 목록 조회
     */
    List<Channel> findChannelList();

    /**
     * 채널 아이디를 기준으로 단일 조회
     */
    Optional<Channel> findChannelById(Long id);

    /**
     * 채널 코드 기준으로 단일 조회
     */
    Optional<Channel> findChannelByCode(String channelCode);

    /**
     * 채널의 휴대전화 등록 시 첫 메시지 업데이트
     */
    Channel updateInviteMessage(Long channelId, String message);

}
