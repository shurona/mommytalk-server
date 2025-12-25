package com.shrona.mommytalk.message.application;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.message.domain.MessageLog;
import com.shrona.mommytalk.message.presentation.dtos.response.MessageHistoryResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {

    /**
     * 메시지 단일 조회
     */
    MessageLog findByMessageId(Long id);

    /**
     * 메시지 상세 조회
     */
    MessageLog findInfoByMessageId(Long id);

    /**
     * 메시지 로그 조회
     */
    Page<MessageLog> findMessageLogList(Channel channel, Pageable pageable);

    /**
     * 예약 된 메시지 목록 조회
     */
    List<MessageLog> findAllByBeforeNow();

    /**
     * 로그의 라인 아이디 갯수를 갖고 온다.
     */
    Map<Long, Integer> findLineIdCountByLog(List<Long> list);

    /**
     * 선택된 그룹에 메시지 전송(제외 그룹 확인)
     */
    List<MessageLog> createMessageSelectGroup(Channel channel, Long selectGroupId,
        List<Long> selectedCustomGroupIds, List<Long> selectedExGroupIds,
        String deliveryDate, LocalDateTime reserveTime, String groupInfo);

    /**
     * 모든 그룹에 메시지 전송(제외 그룹 확인)
     */
    List<MessageLog> createMessageAllGroup
    (Channel channel, List<Long> exceptGroupIds, LocalDateTime reserveTime,
        String groupInfo);


    /**
     * 메시지 캔슬
     */
    Long cancelMessage(Long messageLogId);

    /**
     * 재발송 로직
     */
    void resendMessage(Channel channel, Long messageLogId);

    /**
     * 사용자가 받은 메시지 히스토리 조회 (연월 기준)
     */
    List<MessageHistoryResponseDto> findMessageHistory(
        Long channelId, Long userId, int year, int month);

    /**
     * MessageLog만 생성 (MessageLogDetail 없이, 스케줄 등록 없이) 레거시 용
     */
    MessageLog createMessageLogOnly(Channel channel, Long entitlementGroupId,
        LocalDateTime reserveTime);
}
