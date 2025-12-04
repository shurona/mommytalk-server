package com.shrona.mommytalk.message.application;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.message.domain.MessageLogDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageLogDetailService {

    Page<MessageLogDetail> findLogDetailListByLogId(Long messageLogId, Pageable pageable);

    /**
     * 레거시 MessageLog에 대한 MessageLogDetail 생성
     * (groupInfo='legacy', 특정 entitlementId, userLevel=2/childLevel=2 고정)
     */
    int createLegacyDetails(Channel channel, Long entitlementId);

}
