package com.shrona.mommytalk.message.infrastructure.repository;

import com.shrona.mommytalk.message.domain.MessageLogDetail;
import com.shrona.mommytalk.message.domain.type.ReservationStatus;
import java.util.List;

public interface MessageLogDetailQueryRepository {

    /**
     * ScheduledMessageTextId를 기준으로 Status를 업데이트 한다.
     */
    Long updateStatusByStmId(Long smtId, ReservationStatus status);

    /**
     * status(Optional)에 해당하는 MessageLogDetailInfo 목록을 갖고 온다.
     */
    List<MessageLogDetail> findMldiListByStatusWithLine(
        Long messageLogId, ReservationStatus status);

}
