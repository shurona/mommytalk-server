package com.shrona.mommytalk.message.infrastructure.repository.query;

import static com.shrona.mommytalk.channel.domain.QChannel.channel;
import static com.shrona.mommytalk.group.domain.QGroup.group;
import static com.shrona.mommytalk.message.domain.QMessageLog.messageLog;
import static com.shrona.mommytalk.message.domain.QMessageLogDetail.messageLogDetail;
import static com.shrona.mommytalk.message.domain.type.ReservationStatus.PREPARE;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shrona.mommytalk.message.domain.MessageLog;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class MessageQueryRepositoryImpl implements MessageQueryRepository {

    private final JPAQueryFactory query;

    public List<MessageLog> findAllByReservedMessageBeforeDate(LocalDateTime time) {

        return query.select(messageLog)
            .from(messageLog)
            .leftJoin(messageLog.group, group).fetchJoin()
            .leftJoin(group.channel, channel).fetchJoin()
            .where(messageLog.reserveTime.loe(time)
                .and(messageLog.id.in(
                    JPAExpressions.select(messageLogDetail.messageLog.id)
                        .from(messageLogDetail)
                        .where(messageLogDetail.status.eq(PREPARE))
                )))
            .fetch();
    }

    public List<MessageLog> findMessageByIds(List<Long> messageLogIds) {

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(messageLog.id.in(messageLogIds));

        return query.select(messageLog)
            .from(messageLog)
            .leftJoin(messageLog.channel, channel).fetchJoin()
            .where(builder)
            .fetch();
    }

}
