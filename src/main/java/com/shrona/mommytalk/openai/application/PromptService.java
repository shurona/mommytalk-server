package com.shrona.mommytalk.openai.application;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.openai.domain.MessagePrompt;
import com.shrona.mommytalk.openai.domain.type.PromptType;

public interface PromptService {

    /**
     * 프롬프트 목록 조회
     */
    MessagePrompt findMessagePromptList(Channel channel);

    /**
     * 프롬프트 단일 조회
     */
    MessagePrompt findById(Long promptId);

    /**
     * 새로운 프롬프트를 생성한다.
     */
    Long insertPromptInfo(Channel channel, String label, String prompt, PromptType type);

    /**
     * 프롬프트를 업데이트 한다.
     */
    Long updatePromptInfo(Channel channel, Long promptId, String label, String prompt);

    /**
     * prompt를 사용 등록한다.
     */
    Long registerPromptInfo(Channel channel, Long promptId);

    /**
     * 프롬프트를 삭제 한다.
     */
    Long deletePrompt(Long promptId);


}
