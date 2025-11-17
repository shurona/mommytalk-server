package com.shrona.mommytalk.message.infrastructure.repository.query;

import com.shrona.mommytalk.message.domain.MessageLogDetail;
import com.shrona.mommytalk.message.domain.type.ReservationStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageLogDetailQueryRepository {

    /**
     * messageLog를 기준으로 MessageLogDetail 목록을 갖고 온다.
     */
    Page<MessageLogDetail> findMessageLogDetailListByLogId(Long messageLogId, Pageable pageable);

    /**
     * MessageTemplateId를 기준으로 Status를 업데이트 한다.
     */
    Long updateStatusByContentId(
        Long messageContentId, Long messageLogId, ReservationStatus status);

    /**
     * status(Optional)에 해당하는 MessageLogDetailInfo 목록을 갖고 온다. (LINE용)
     */
    List<MessageLogDetail> findMldListByStatusWithLine(
        Long messageLogId, List<ReservationStatus> status);

    /**
     * status(Optional)에 해당하는 MessageLogDetailInfo 목록을 갖고 온다. (KAKAO용)
     */
    List<MessageLogDetail> findMldListByStatusWithKakao(
        Long messageLogId, List<ReservationStatus> status);

    /**
     * 메시지 LogId에 해당하는 것중 complete 이외에 모두 cancel
     */
    void cancelDetailByLogId(Long messageLogId);

    /**
     * 사용자가 받은 메시지 히스토리 조회 (채널 및 연월 필터링, 날짜 내림차순)
     */
    List<MessageLogDetail> findMessageHistoryByUserAndYearMonth(Long channelId, Long userId,
        int year, int month);

    /**
     * 사용자가 받은 특정 메시지 컨텐츠 조회 (Entitlement JOIN 포함)
     */
    MessageLogDetail findByChannelAndUserAndContent(
        Long channelId, Long userId, Long messageLogDetailId);

}
