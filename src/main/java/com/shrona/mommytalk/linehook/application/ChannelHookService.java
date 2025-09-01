package com.shrona.mommytalk.linehook.application;

import com.shrona.mommytalk.line.domain.ChannelUserConnection;

public interface ChannelHookService {

    /**
     * LineId의 상태를 Follow를 true로 변경해준다.
     */
    public ChannelUserConnection followLineUserByLineId(Long channelId, String lineId);


    /**
     * LineId의 상태를 Follow를 False로 변경해준다.
     */
    public void unfollowLineUserByLineId(Long channelId, String lineId);

    /**
     * 라인 친구 추가 이후에 메시지를 기록할 때 저장해주는 메소드(휴대전화인 경우에만 True를 내려줘서 이후 로직 실행)
     */
    public boolean saveLineMessage(Long channelId, String lineId, String content);

    /**
     * 휴대전화가 제대로 등록된 경우에는 메시지를 보내준다.
     */
    public void sendLineMessageAfterSuccess(Long channelId, String lineId, String phoneNumber);

}
