package com.shrona.mommytalk.message.infrastructure.repository.query;

import com.shrona.mommytalk.message.domain.MessageLogDetail;
import com.shrona.mommytalk.message.domain.type.ReservationStatus;
import java.util.List;

public interface MessageLogDetailQueryRepository {

    /**
     * MessageTemplateId를 기준으로 Status를 업데이트 한다.
     */
    Long updateStatusByStmId(Long smtId, Long messageLogId, ReservationStatus status);

    /**
     * status(Optional)에 해당하는 MessageLogDetailInfo 목록을 갖고 온다.
     */
    List<MessageLogDetail> findMldListByStatusWithLine(
        Long messageLogId, ReservationStatus status);

}
