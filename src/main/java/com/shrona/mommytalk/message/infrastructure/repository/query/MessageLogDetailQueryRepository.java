package com.shrona.mommytalk.message.infrastructure.repository.query;

import com.shrona.mommytalk.message.domain.MessageLogDetail;
import com.shrona.mommytalk.message.domain.type.ReservationStatus;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
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
     *
     * @param sendTimeBefore 유저 선호 발송 시간(KST)이 이 시간 이전인 대상만 조회 (null이면 전체)
     */
    List<MessageLogDetail> findMldListByStatusWithKakao(
        Long messageLogId, List<ReservationStatus> status, LocalTime sendTimeBefore);

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


    /**
     * MessageLogId를 기준으로 MessageLogDetail에 이미 있는 유저 Id 목록을 조회한다.
     */
    Set<Long> findUserIdsByMessageLogId(Long messageLogId);

    /**
     * MessageLogDetail ID 목록으로 상태 일괄 업데이트 (Batch UPDATE)
     */
    void updateStatusByIds(List<Long> messageLogDetailIds, ReservationStatus status);

    /**
     * 특정 유저의 미발송(PREPARE) Detail 중 reserveTime이 미래인 건을 EXPIRED로 일괄 변경한다. (사용권 만료 처리용)
     */
    long expireFutureDetailsByUserAndEntitlement(Long userId, Long entitlementId,
        LocalDateTime now);

    /**
     * MessageLog 내 특정 유저들의 EXPIRED 상태 Detail ID 목록을 조회한다. (재활성 유저 복구용)
     */
    List<Long> findExpiredDetailIdsByLogIdAndUserIds(Long messageLogId, List<Long> userIds);
}
