package com.shrona.mommytalk.message.infrastructure.repository;

import static com.shrona.mommytalk.channel.domain.QChannel.channel;
import static com.shrona.mommytalk.group.domain.QGroup.group;
import static com.shrona.mommytalk.message.domain.QMessageLog.messageLog;

import com.querydsl.core.BooleanBuilder;
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

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(messageLog.reserveTime.loe(time)); // 현재보다 이전 상태

        return query.select(messageLog)
            .from(messageLog)
            .leftJoin(messageLog.group, group).fetchJoin()
            .leftJoin(group.channel, channel).fetchJoin()
            .where(builder)
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
