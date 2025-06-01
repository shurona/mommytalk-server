package com.shrona.line_demo.linehook.application;

import com.shrona.line_demo.line.domain.ChannelLineUser;

public interface ChannelHookService {

    /**
     * LineId의 상태를 Follow를 true로 변경해준다.
     */
    public ChannelLineUser followLineUserByLineId(Long channelId, String lineId);


    /**
     * LineId의 상태를 Follow를 False로 변경해준다.
     */
    public void unfollowLineUserByLineId(Long channelId, String lineId);

    /**
     * 라인 친구 추가 이후에 메시지를 기록할 때 저장해주는 메소드
     */
    public void saveLineMessage(Long channelId, String lineId, String content);

}
