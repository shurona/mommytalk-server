package com.shrona.mommytalk.message.infrastructure.repository.query;

import static com.shrona.mommytalk.kakao.domain.QKakaoUser.kakaoUser;
import static com.shrona.mommytalk.line.domain.QLineUser.lineUser;
import static com.shrona.mommytalk.message.domain.QMessageContent.messageContent;
import static com.shrona.mommytalk.message.domain.QMessageLogDetail.messageLogDetail;
import static com.shrona.mommytalk.user.domain.QUser.user;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shrona.mommytalk.elevenlabs.domain.QElevenLabsMedia;
import com.shrona.mommytalk.message.domain.MessageLogDetail;
import com.shrona.mommytalk.message.domain.type.ReservationStatus;
import java.util.List;
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

        // 가져온 ID 목록으로 실제 데이터를 로드한다.
        List<MessageLogDetail> fetch = query.selectFrom(messageLogDetail)
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
}
