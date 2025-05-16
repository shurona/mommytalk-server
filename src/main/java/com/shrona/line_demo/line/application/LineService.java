package com.shrona.line_demo.line.application;

import com.shrona.line_demo.line.domain.LineUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LineService {

    public LineUser findLineUserByLineId(String lineId);

    public Page<LineUser> findLineUserList(Pageable pageable);

    /**
     * 라인 친구 추가 이후에 메시지를 기록할 때 저장해주는 메소드
     */
    public void saveLineMessage(String lineId, String content);

    /**
     * LineId의 상태를 Follow를 true로 변경해준다.
     */
    public LineUser followLineUserByLineId(String lineId);


    /**
     * LineId의 상태를 Follow를 False로 변경해준다.
     */
    public void unfollowLineUserByLineId(String lineId);

}
