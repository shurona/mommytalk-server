package com.shrona.mommytalk.message.application;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.message.domain.MessageLogDetail;
import com.shrona.mommytalk.message.domain.type.ReservationStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageLogDetailService {

    Page<MessageLogDetail> findLogDetailListByLogId(Long messageLogId, Pageable pageable);

    /**
     * 레거시 MessageLog에 대한 MessageLogDetail 생성
     * (groupInfo='legacy', 특정 entitlementId, userLevel=2/childLevel=2 고정)
     */
    int createLegacyDetails(Channel channel, Long entitlementId);


    /**
     * 예약 메시지 발송 전 누락 유저 추가 및 재활성 유저의 EXPIRED Detail 복구
     * 발송 트랜잭션과 분리된 새 트랜잭션에서 커밋한다.
     * 발송 결과 상태 갱신(REQUIRES_NEW)이 여기서 추가한 상세를 볼 수 있어야 하기 때문이다.
     */
    int addMissingDetailsBeforeSend(Long messageLogId);

    /**
     * 발송 접수 결과 상태 변경 (새 트랜잭션에서 즉시 커밋)
     * 발송 루프 도중 장애가 발생해도 이미 접수된 건의 상태가 유실되지 않도록 청크 단위로 커밋한다.
     */
    void updateStatusByIds(List<Long> messageLogDetailIds, ReservationStatus status);

}
