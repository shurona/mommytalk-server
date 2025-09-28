package com.shrona.mommytalk.message.infrastructure.repository.query;

import static com.shrona.mommytalk.line.domain.QLineUser.lineUser;
import static com.shrona.mommytalk.message.domain.QMessageLogDetail.messageLogDetail;
import static com.shrona.mommytalk.user.domain.QUser.user;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shrona.mommytalk.message.domain.MessageLogDetail;
import com.shrona.mommytalk.message.domain.type.ReservationStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class MessageLogDetailQueryRepositoryImpl implements
    MessageLogDetailQueryRepository {

    private final JPAQueryFactory query;

    @Override
    public Long updateStatusByStmId(Long smtId, ReservationStatus status) {

        return query.update(messageLogDetail)
            .set(messageLogDetail.status, status)
            .where(messageLogDetail.messageContent.id.eq(smtId))
            .execute();
    }

    public List<MessageLogDetail> findMldiListByStatusWithLine(
        Long messageLogId, ReservationStatus status) {

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(messageLogDetail.messageLog.id.eq(messageLogId));

        builder.and(messageLogDetail.user.lineUser.isNotNull());

        if (status != null) {
            builder.and(messageLogDetail.status.eq(status));
        }

        return query.select(messageLogDetail)
            .from(messageLogDetail)
            .leftJoin(messageLogDetail.user, user).fetchJoin()
            .leftJoin(user.lineUser, lineUser).fetchJoin()
            .where(builder)
            .fetch();
    }
}
