package com.shrona.mommytalk.message.infrastructure.repository.query;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.message.domain.MessageLog;
import com.shrona.mommytalk.message.presentation.dtos.response.AvailableDateResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.MessageLogResponseDto;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageLogQueryRepository {

    MessageLog findMessageLogById(Long messageId);

    /**
     * 채널에서 9개 컨텐츠(3x3 레벨)가 모두 승인된 MessageType 조회 (기간 내) + messageCount 포함
     */
    List<AvailableDateResponseDto> findAvailableMessageTypesWithFullApprovedContent(
        Channel channel, LocalDate startDate, LocalDate endDate);

    /**
     * 채널별 MessageLog 목록을 페이징으로 조회 (상태 포함)
     */
    Page<MessageLogResponseDto> findMessageLogsByChannel(Long channelId, Pageable pageable);

}
