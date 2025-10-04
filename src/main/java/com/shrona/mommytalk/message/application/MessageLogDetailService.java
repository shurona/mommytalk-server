package com.shrona.mommytalk.message.application;

import com.shrona.mommytalk.message.domain.MessageLogDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageLogDetailService {

    Page<MessageLogDetail> findLogDetailListByLogId(Long messageLogId, Pageable pageable);

}
