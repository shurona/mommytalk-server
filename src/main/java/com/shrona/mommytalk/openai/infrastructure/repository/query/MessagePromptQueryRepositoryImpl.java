package com.shrona.mommytalk.openai.infrastructure.repository.query;

import static com.shrona.mommytalk.openai.domain.QMessagePrompt.messagePrompt;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.openai.domain.MessagePrompt;
import com.shrona.mommytalk.openai.domain.type.PromptType;
import com.shrona.mommytalk.openai.infrastructure.repository.dto.PromptHistoryDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class MessagePromptQueryRepositoryImpl implements MessagePromptQueryRepository {

    private final JPAQueryFactory query;

    @Override
    public List<MessagePrompt> findSelectedPromptList(Channel channel) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(messagePrompt.selected.isTrue());

        return query.selectFrom(messagePrompt)
            .where(builder)
            .fetch();
    }

    @Override
    public MessagePrompt findSelectedPromptByChannelAndType(Channel channel, PromptType type) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(messagePrompt.selected.isTrue());
        builder.and(messagePrompt.type.eq(type));

        return query.selectFrom(messagePrompt)
            .where(builder)
            .fetchOne();
    }

    @Override
    public List<PromptHistoryDto> findPromptHistoryList(Channel channel, PromptType type) {

        BooleanBuilder builder = new BooleanBuilder();
//        builder.and(messagePrompt.selected.isFalse());
        builder.and(messagePrompt.type.eq(type));

        return query.select(
                Projections.constructor(
                    PromptHistoryDto.class,
                    messagePrompt.id,
                    messagePrompt.label,
                    messagePrompt.selected,
                    messagePrompt.createdAt
                )
            )
            .from(messagePrompt)
            .orderBy(messagePrompt.id.asc())
            .where(builder)
            .fetch();
    }
}
