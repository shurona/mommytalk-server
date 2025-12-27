package com.shrona.mommytalk.message.infrastructure.repository.query;

import static com.shrona.mommytalk.kakao.domain.QKakaoUser.kakaoUser;
import static com.shrona.mommytalk.line.domain.QLineUser.lineUser;
import static com.shrona.mommytalk.message.domain.QMessageContent.messageContent;
import static com.shrona.mommytalk.message.domain.QMessageLog.messageLog;
import static com.shrona.mommytalk.message.domain.QMessageLogDetail.messageLogDetail;
import static com.shrona.mommytalk.user.domain.QUser.user;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shrona.mommytalk.elevenlabs.domain.QElevenLabsMedia;
import com.shrona.mommytalk.message.domain.MessageLogDetail;
import com.shrona.mommytalk.message.domain.type.ReservationStatus;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Repository
public class MessageLogDetailQueryRepositoryImpl implements
    MessageLogDetailQueryRepository {

    private final JPAQueryFactory query;

    @Override
    public Page<MessageLogDetail> findMessageLogDetailListByLogId(
        Long messageLogId, Pageable pageable) {

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(messageLogDetail.messageLog.id.eq(messageLogId));

        List<MessageLogDetail> fetch = query.selectFrom(messageLogDetail)
            .leftJoin(messageLogDetail.messageContent, messageContent).fetchJoin()
            .leftJoin(messageLogDetail.user, user)
            .leftJoin(user.lineUser, lineUser)
            .where(builder)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(messageLogDetail.createdAt.desc())
            .fetch();

        JPAQuery<Long> total = query.select(messageLogDetail.count())
            .from(messageLogDetail)
            .where(builder);

        return PageableExecutionUtils.getPage(fetch, pageable, total::fetchOne);
    }

    @Transactional
    public Long updateStatusByContentId(Long messageContentId, Long messageLogId,
        ReservationStatus status) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(messageLogDetail.messageContent.id.eq(messageContentId));
        builder.and(messageLogDetail.messageLog.id.eq(messageLogId));

        return query.update(messageLogDetail)
            .set(messageLogDetail.status, status)
            .where(builder)
            .execute();
    }

    public List<MessageLogDetail> findMldListByStatusWithLine(
        Long messageLogId, List<ReservationStatus> status) {

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(messageLogDetail.messageLog.id.eq(messageLogId));

        builder.and(messageLogDetail.user.lineUser.isNotNull());

        if (status != null) {
            builder.and(messageLogDetail.status.in(status));
        }

        // 다른 별칭 사용
        QElevenLabsMedia headerOneMedia = new QElevenLabsMedia("headerOneMedia");
        QElevenLabsMedia headerTwoMedia = new QElevenLabsMedia("headerTwoMedia");

        return query.select(messageLogDetail)
            .from(messageLogDetail)
            .leftJoin(messageLogDetail.user, user).fetchJoin()
            .leftJoin(user.lineUser, lineUser).fetchJoin()
            .leftJoin(messageLogDetail.messageContent, messageContent).fetchJoin()
            .leftJoin(messageContent.headerOneLink, headerOneMedia).fetchJoin()
            .leftJoin(messageContent.headerTwoLink, headerTwoMedia).fetchJoin()
            .where(builder)
            .fetch();
    }

    public List<MessageLogDetail> findMldListByStatusWithKakao(
        Long messageLogId, List<ReservationStatus> status) {

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(messageLogDetail.messageLog.id.eq(messageLogId));

        // 카카오 유저가 있고, 전화번호가 있는 사용자만 조회
        builder.and(messageLogDetail.user.kakaoUser.isNotNull());
        builder.and(messageLogDetail.user.phoneNumber.isNotNull());

        if (status != null) {
            builder.and(messageLogDetail.status.in(status));
        }

        // 다른 별칭 사용
        QElevenLabsMedia headerOneMedia = new QElevenLabsMedia("headerOneMedia");
        QElevenLabsMedia headerTwoMedia = new QElevenLabsMedia("headerTwoMedia");

        return query.select(messageLogDetail)
            .from(messageLogDetail)
            .leftJoin(messageLogDetail.user, user).fetchJoin()
            .leftJoin(user.kakaoUser, kakaoUser).fetchJoin()
            .leftJoin(messageLogDetail.messageContent, messageContent).fetchJoin()
            .leftJoin(messageContent.headerOneLink, headerOneMedia).fetchJoin()
            .leftJoin(messageContent.headerTwoLink, headerTwoMedia).fetchJoin()
            .where(builder)
            .fetch();
    }

    @Transactional
    public void cancelDetailByLogId(Long messageLogId) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(messageLogDetail.messageLog.id.eq(messageLogId));
        builder.and(messageLogDetail.status.notIn(ReservationStatus.COMPLETE));

        query.update(messageLogDetail)
            .set(messageLogDetail.status, ReservationStatus.CANCEL)
            .where(builder)
            .execute();
    }

    @Override
    public List<MessageLogDetail> findMessageHistoryByUserAndYearMonth(Long channelId, Long userId,
        int year, int month) {
        BooleanBuilder builder = new BooleanBuilder();

        // channelId 필터링 (MessageLog의 channel)
        builder.and(messageLogDetail.messageLog.channel.id.eq(channelId));

        // userId 필터링
        builder.and(messageLogDetail.user.id.eq(userId));

        // 연월 필터링 (deliveryTime이 해당 연월에 속하는지)
        builder.and(messageLogDetail.messageContent.messageType.deliveryTime.year().eq(year));
        builder.and(messageLogDetail.messageContent.messageType.deliveryTime.month().eq(month));
        builder.and(messageLogDetail.status.eq(ReservationStatus.COMPLETE));

        // MessageType과 MessageContent JOIN FETCH로 N+1 방지
        return query.select(messageLogDetail)
            .from(messageLogDetail)
            .leftJoin(messageLogDetail.messageContent, messageContent).fetchJoin()
            .leftJoin(messageContent.messageType).fetchJoin()
            .where(builder)
            .orderBy(messageContent.messageType.deliveryTime.desc())
            .fetch();
    }

    @Override
    public MessageLogDetail findByChannelAndUserAndContent(
        Long channelId, Long userId, Long messageLogDetailId) {
        BooleanBuilder builder = new BooleanBuilder();

        // channelId 필터링 (MessageLog의 channel)
        builder.and(messageLogDetail.messageLog.channel.id.eq(channelId));

        // userId 필터링
        builder.and(messageLogDetail.user.id.eq(userId));

        // messageLogDetailId 필터링
        builder.and(messageLogDetail.id.eq(messageLogDetailId));

        // COMPLETE 상태만 조회
        builder.and(messageLogDetail.status.eq(ReservationStatus.COMPLETE));

        // MessageLog, Entitlement, MessageContent, MessageType JOIN FETCH로 N+1 방지
        return query.select(messageLogDetail)
            .from(messageLogDetail)
            .leftJoin(messageLogDetail.messageLog, messageLog).fetchJoin()
            .leftJoin(messageLog.entitlement).fetchJoin()
            .leftJoin(messageLogDetail.messageContent, messageContent).fetchJoin()
            .leftJoin(messageContent.messageType).fetchJoin()
            .leftJoin(messageContent.headerOneLink).fetchJoin()
            .leftJoin(messageContent.headerTwoLink).fetchJoin()
            .where(builder)
            .fetchOne();
    }

    @Override
    public Set<Long> findUserIdsByMessageLogId(Long messageLogId) {
        List<Long> userIds = query.select(messageLogDetail.user.id)
            .from(messageLogDetail)
            .where(messageLogDetail.messageLog.id.eq(messageLogId))
            .fetch();

        return Set.copyOf(userIds);
    }

    @Override
    @Transactional
    public void updateStatusByIds(List<Long> messageLogDetailIds, ReservationStatus status) {
        if (messageLogDetailIds == null || messageLogDetailIds.isEmpty()) {
            return;
        }

        query.update(messageLogDetail)
            .set(messageLogDetail.status, status)
            .where(messageLogDetail.id.in(messageLogDetailIds))
            .execute();
    }
}
