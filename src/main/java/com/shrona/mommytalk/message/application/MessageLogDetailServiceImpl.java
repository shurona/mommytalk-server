package com.shrona.mommytalk.message.application;

import com.shrona.mommytalk.message.domain.MessageLogDetail;
import com.shrona.mommytalk.message.infrastructure.repository.query.MessageLogDetailQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MessageLogDetailServiceImpl implements MessageLogDetailService {

    private final MessageLogDetailQueryRepository messageLogDetailQueryRepository;

    @Override
    public Page<MessageLogDetail> findLogDetailListByLogId(Long messageLogId, Pageable pageable) {
        return messageLogDetailQueryRepository.findMessageLogDetailListByLogId(
            messageLogId, pageable);
    }
}
