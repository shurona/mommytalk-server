package com.shrona.mommytalk.message.infrastructure.repository;

import com.shrona.mommytalk.message.domain.MessageLog;
import java.util.List;

public interface MessageQueryRepository {

    /**
     * 현재 이전에 적용 된 메시지 목록 갖고 온다.
     */
    public List<MessageLog> findAllByReservedMessageBeforeNow();

}
