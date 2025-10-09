package com.shrona.mommytalk.openai.infrastructure.repository.query;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.openai.domain.MessagePrompt;
import com.shrona.mommytalk.openai.domain.type.PromptType;
import com.shrona.mommytalk.openai.infrastructure.repository.dto.PromptHistoryDto;
import java.util.List;

public interface MessagePromptQueryRepository {


    /**
     * 채널과 type에서 선택된 프롬프트를 갖고 온다
     */
    MessagePrompt findSelectedPromptByChannelAndType(Channel channel, PromptType type);

    /**
     * 선택된 프롬프트 목록을 갖고 온다.
     */
    List<MessagePrompt> findSelectedPromptList(Channel channel);


    /**
     * 프롬프트 버전 히스토리를 갖고 온다.
     */
    List<PromptHistoryDto> findPromptHistoryList(Channel channel, PromptType type);

}
