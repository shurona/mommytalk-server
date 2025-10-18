package com.shrona.mommytalk.message.infrastructure.repository.query;

import static com.shrona.mommytalk.message.domain.QMessageContent.messageContent;
import static com.shrona.mommytalk.message.domain.QMessageType.messageType;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shrona.mommytalk.elevenlabs.domain.QElevenLabsMedia;
import com.shrona.mommytalk.message.domain.MessageContent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class MessageContentQueryRepositoryImpl implements MessageContentQueryRepository {

    private final JPAQueryFactory query;

    @Override
    public MessageContent findById(Long id) {

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(messageContent.id.eq(id));

        // 다른 별칭 사용
        QElevenLabsMedia headerOneMedia = new QElevenLabsMedia("headerOneMedia");
        QElevenLabsMedia headerTwoMedia = new QElevenLabsMedia("headerTwoMedia");

        return query.select(messageContent)
            .from(messageContent)
            .leftJoin(messageContent.messageType, messageType).fetchJoin()
            .leftJoin(messageContent.headerOneLink, headerOneMedia).fetchJoin()
            .leftJoin(messageContent.headerTwoLink, headerTwoMedia).fetchJoin()
            .where(builder)
            .fetchOne();
    }
}
