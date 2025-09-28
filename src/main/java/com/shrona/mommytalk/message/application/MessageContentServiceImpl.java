package com.shrona.mommytalk.message.application;

import static com.shrona.mommytalk.message.common.exception.MessageErrorCode.MESSAGE_CONTENT_ACCESS_DENIED;
import static com.shrona.mommytalk.message.common.exception.MessageErrorCode.MESSAGE_CONTENT_NOT_FOUND;
import static com.shrona.mommytalk.message.common.exception.MessageErrorCode.MESSAGE_PROMPT_NOT_EXIST;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.message.common.exception.MessageException;
import com.shrona.mommytalk.message.domain.MessageContent;
import com.shrona.mommytalk.message.domain.MessageType;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageContentJpaRepository;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageTypeJpaRepository;
import com.shrona.mommytalk.message.presentation.dtos.request.AiGenerateRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.UpdateTemplateRequestDto;
import com.shrona.mommytalk.openai.application.OpenAiServiceImpl;
import com.shrona.mommytalk.openai.domain.MessagePrompt;
import com.shrona.mommytalk.openai.infrastructure.repository.MessagePromptJpaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MessageContentServiceImpl implements MessageContentService {

    private static int MIN_LEVEL = 1;
    private static int MAX_LEVEL = 3;

    private final MessagePromptJpaRepository messagePromptJpaRepository;
    private final MessageTypeJpaRepository messageTypeJpaRepository;
    private final MessageContentJpaRepository messageContentJpaRepository;
    private final OpenAiServiceImpl openAiService;

    @Override
    @Transactional
    public MessageContent generateAiContent(Channel channel, AiGenerateRequestDto requestDto) {

        // 1. MessageType 조회 또는 생성
        MessageType messageType = messageTypeJpaRepository
            .findByChannelAndDeliveryTime(channel, requestDto.deliveryDate())
            .orElseGet(() -> {
                MessageType newType = MessageType.of(
                    requestDto.theme(),
                    requestDto.context(),
                    requestDto.deliveryDate(),
                    channel
                );
                return messageTypeJpaRepository.save(newType);
            });

        // 기존 MessageType이 있으면 내용 업데이트
        messageType.updateContent(requestDto.theme(), requestDto.context());
        messageTypeJpaRepository.save(messageType);

        // 2. 동일한 레벨의 MessageContent가 이미 존재하는지 확인
        Optional<MessageContent> existingContent = messageContentJpaRepository
            .findByMessageTypeAndChildLevelAndUserLevel(
                messageType, requestDto.childLevel(), requestDto.userLevel());

        // regenerate=false이고 기존 컨텐츠가 있으면 기존 컨텐츠 반환
        if (existingContent.isPresent() && !Boolean.TRUE.equals(requestDto.regenerate())) {
            return existingContent.get();
        }

        // 3. 메시지 프롬프트를 갖고 온다
        MessagePrompt messagePrompt = messagePromptJpaRepository.findByChannel(channel)
            .orElseThrow(() -> new MessageException(MESSAGE_PROMPT_NOT_EXIST));

        // 4. OpenAI 프롬프트 생성 및 API 호출
        String prompt = openAiService.buildMommyTalkPrompt(
            messagePrompt.getPrompt(),
            requestDto.theme(),
            requestDto.context(),
            requestDto.userLevel(),
            requestDto.childLevel()
        );

        String generatedContent = openAiService.generateData(prompt);

        // 5. MessageContent 생성 또는 업데이트
        if (existingContent.isPresent() && requestDto.regenerate()) {
            // regenerate=true이고 기존 컨텐츠가 있으면 업데이트
            MessageContent existingMessageContent = existingContent.get();
            existingMessageContent.updateContent(generatedContent,
                existingMessageContent.getDiaryUrl());
            return messageContentJpaRepository.save(existingMessageContent);
        } else {
            // 새 컨텐츠 생성
            MessageContent messageContent = MessageContent.ofWithMockUrls(
                messageType,
                generatedContent,
                requestDto.childLevel(),
                requestDto.userLevel()
            );
            return messageContentJpaRepository.save(messageContent);
        }
    }

    @Override
    @Transactional
    public void updateMessageContent(Long channelId, Long contentId,
        UpdateTemplateRequestDto requestDto) {

        // 1. MessageContent 조회
        MessageContent messageContent = messageContentJpaRepository.findById(contentId)
            .orElseThrow(() -> new MessageException(MESSAGE_CONTENT_NOT_FOUND));

        // 2. 채널 권한 검증 (content의 messageType의 channel이 요청한 channelId와 일치하는지)
        if (!messageContent.getMessageType().getChannel().getId().equals(channelId)) {
            throw new MessageException(MESSAGE_CONTENT_ACCESS_DENIED);
        }

        // 3. 컨텐츠 업데이트 (vocaUrl은 무시)
        messageContent.updateContent(requestDto.messageText(), requestDto.diaryUrl());

        // 4. 저장
        messageContentJpaRepository.save(messageContent);
    }

    @Override
    @Transactional
    public void approveMessageContent(Long channelId, Long contentId) {

        // 1. MessageContent 조회
        MessageContent messageContent = messageContentJpaRepository.findById(contentId)
            .orElseThrow(() -> new MessageException(MESSAGE_CONTENT_NOT_FOUND));

        // 2. 채널 권한 검증 (content의 messageType의 channel이 요청한 channelId와 일치하는지)
        if (!messageContent.getMessageType().getChannel().getId().equals(channelId)) {
            throw new MessageException(MESSAGE_CONTENT_ACCESS_DENIED);
        }

        // 3. 승인 처리 (이미 승인된 경우 DB 업데이트 안함)
        boolean needsUpdate = messageContent.approve();

        // 4. 필요한 경우에만 저장 (성능 최적화)
        if (needsUpdate) {
            messageContentJpaRepository.save(messageContent);
        }
    }
}