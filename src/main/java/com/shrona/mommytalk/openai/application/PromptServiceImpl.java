package com.shrona.mommytalk.openai.application;

import static com.shrona.mommytalk.message.common.exception.MessageErrorCode.REGISTERED_MESSAGE_PROMPT;
import static com.shrona.mommytalk.openai.common.exception.PromptErrorCode.MESSAGE_PROMPT_NOT_FOUND;
import static com.shrona.mommytalk.openai.common.exception.PromptErrorCode.PROMPT_NOT_FOUND;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.message.common.exception.MessageException;
import com.shrona.mommytalk.openai.common.exception.PromptException;
import com.shrona.mommytalk.openai.domain.MessagePrompt;
import com.shrona.mommytalk.openai.domain.type.PromptType;
import com.shrona.mommytalk.openai.infrastructure.repository.jpa.MessagePromptJpaRepository;
import com.shrona.mommytalk.openai.infrastructure.repository.query.MessagePromptQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class PromptServiceImpl implements PromptService {

    private final MessagePromptJpaRepository messagePromptRepository;
    private final MessagePromptQueryRepository messagePromptQueryRepository;


    @Override
    public MessagePrompt findMessagePromptList(Channel channel) {

        return messagePromptRepository.findByChannel(channel)
            .orElseThrow(() -> new PromptException(MESSAGE_PROMPT_NOT_FOUND));
    }

    @Override
    public MessagePrompt findById(Long promptId) {
        return messagePromptRepository.findById(promptId)
            .orElseThrow(() -> new PromptException(PROMPT_NOT_FOUND));
    }

    @Transactional
    public Long insertPromptInfo(Channel channel, String label, String prompt, PromptType type) {

        MessagePrompt messagePrompt = messagePromptQueryRepository.findSelectedPromptByChannelAndType(
            channel, PromptType.BASIC);

        if (messagePrompt != null) {
            messagePrompt.disablePrompt();
            messagePromptRepository.save(messagePrompt);
        }

        // 새로운 프롬프트 생성
        MessagePrompt promptInfo = messagePromptRepository.save(
            MessagePrompt.of(channel, prompt, label, type));
        return promptInfo.getId();
    }

    @Transactional
    public Long updatePromptInfo(Channel channel, Long promptId, String label, String prompt) {

        MessagePrompt messagePrompt = messagePromptRepository.findById(promptId)
            .orElseThrow(() -> new PromptException(PROMPT_NOT_FOUND));
        messagePrompt.updateWhenRegister(
            prompt, label
        );

        return promptId;
    }

    @Transactional
    public Long registerPromptInfo(Channel channel, Long promptId) {

        MessagePrompt messagePrompt = messagePromptRepository.findById(promptId)
            .orElseThrow(() -> new PromptException(PROMPT_NOT_FOUND));

        // 만약 현재 수정되는 것이 선택된 것이 아니면 기존 프롬프트를 비활성화 한다.
        if (!messagePrompt.getSelected()) {
            disableExistPrompt(channel, messagePrompt.getType());
        }
        // 요청 받은 MessagePrompt를 등록한다.
        messagePrompt.registerPrompt();
        return messagePrompt.getId();
    }

    @Transactional
    public Long deletePrompt(Long promptId) {

        MessagePrompt messagePrompt = messagePromptRepository.findById(promptId)
            .orElseThrow(() -> new PromptException(PROMPT_NOT_FOUND));

        if (messagePrompt.getIsDeleted()) {
            throw new MessageException(REGISTERED_MESSAGE_PROMPT);
        }

        messagePrompt.deletePrompt();

        return messagePrompt.getId();
    }

    /**
     * 프롬프트를 비활성화 한다.
     */
    private void disableExistPrompt(Channel channel, PromptType type) {
        MessagePrompt messagePrompt = messagePromptQueryRepository.findSelectedPromptByChannelAndType(
            channel, type);

        if (messagePrompt == null) {
            return;
        }

        messagePrompt.disablePrompt();
        messagePromptRepository.save(messagePrompt);
    }

}
