package com.shrona.mommytalk.message.infrastructure.repository;

import static com.shrona.mommytalk.group.domain.QGroup.group;
import static com.shrona.mommytalk.message.domain.QMessageLog.messageLog;
import static com.shrona.mommytalk.message.domain.QMessageType.messageType;
import static com.shrona.mommytalk.message.domain.QScheduledMessageText.scheduledMessageText;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shrona.mommytalk.message.domain.MessageLog;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class MessageQueryRepositoryImpl implements MessageQueryRepository {

    private final JPAQueryFactory query;

    public List<MessageLog> findAllByReservedMessageBeforeNow() {
        return query.select(messageLog)
            .from(messageLog)
            .leftJoin(messageLog.group, group).fetchJoin()
            .leftJoin(messageLog.messageType, messageType).fetchJoin()
            .leftJoin(messageType.scheduledMessageTextList, scheduledMessageText).fetchJoin()
            .fetch();
    }

}
