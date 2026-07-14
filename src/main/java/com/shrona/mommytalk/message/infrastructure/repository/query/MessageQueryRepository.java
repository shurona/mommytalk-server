package com.shrona.mommytalk.message.infrastructure.repository.query;

import com.shrona.mommytalk.message.domain.MessageLog;
import java.time.LocalDateTime;
import java.util.List;

public interface MessageQueryRepository {

    /**
     * 현재 이전에 적용 된 메시지 목록 갖고 온다.
     */
    List<MessageLog> findAllByReservedMessageBeforeDate(LocalDateTime time);

    /**
     * 메시지 로그에 저장된 라인 전송 목록을 갖고 온다.
     */
    public List<MessageLog> findMessageByIds(List<Long> messageLogIds);

    /**
     * 예약 시간(UTC)이 범위 내이고 PREPARE 상태 상세가 남아있는 카카오 채널 MessageLog 목록을 갖고 온다.
     */
    List<MessageLog> findKakaoLogsByReserveTimeRange(LocalDateTime start, LocalDateTime end);

}
