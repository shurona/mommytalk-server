package com.shrona.mommytalk.message.application;

import static com.shrona.mommytalk.message.common.exception.MessageErrorCode.MESSAGE_CONTENT_ACCESS_DENIED;
import static com.shrona.mommytalk.message.common.exception.MessageErrorCode.MESSAGE_CONTENT_NOT_FOUND;
import static com.shrona.mommytalk.message.common.exception.MessageErrorCode.MESSAGE_TYPE_NOT_FOUND;
import static com.shrona.mommytalk.message.common.exception.MessageErrorCode.NEED_MORE_DATE_FOR_APPROVED;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.cloudflare.application.CloudflareService;
import com.shrona.mommytalk.elevenlabs.application.ElevenLabsService;
import com.shrona.mommytalk.elevenlabs.domain.ElevenLabsMedia;
import com.shrona.mommytalk.elevenlabs.infrastructure.reposiotry.ElevenLabsMediaRepository;
import com.shrona.mommytalk.entitlement.domain.EntitlementType;
import com.shrona.mommytalk.message.common.exception.MessageException;
import com.shrona.mommytalk.message.domain.MessageContent;
import com.shrona.mommytalk.message.domain.MessageLogDetail;
import com.shrona.mommytalk.message.domain.MessageType;
import com.shrona.mommytalk.message.domain.type.AudioRole;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageContentJpaRepository;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageTypeJpaRepository;
import com.shrona.mommytalk.message.infrastructure.repository.query.MessageContentQueryRepository;
import com.shrona.mommytalk.message.infrastructure.repository.query.MessageLogDetailQueryRepository;
import com.shrona.mommytalk.message.presentation.dtos.request.AiGenerateRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.ContentAudioRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.UpsertMessageContentRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.response.ContentStatusResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.MessageContentResponseDto;
import com.shrona.mommytalk.openai.application.OpenAiServiceImpl;
import com.shrona.mommytalk.openai.domain.MessagePrompt;
import com.shrona.mommytalk.openai.domain.type.PromptType;
import com.shrona.mommytalk.openai.infrastructure.repository.query.MessagePromptQueryRepository;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class MessageContentServiceImpl implements MessageContentService {

    private final MessagePromptQueryRepository messagePromptQueryRepository;
    private final MessageTypeJpaRepository messageTypeJpaRepository;
    private final MessageContentJpaRepository messageContentJpaRepository;
    private final MessageContentQueryRepository messageContentQueryRepository;
    private final MessageLogDetailQueryRepository messageLogDetailQueryRepository;

    private final ElevenLabsService elevenLabsService;
    private final ElevenLabsMediaRepository elevenLabsMediaRepository;
    private final CloudflareService cloudflareService;
    private final OpenAiServiceImpl openAiService;

    @Override
    public MessageContent findById(Long id) {
        return messageContentQueryRepository.findById(id);
    }

    @Override
    public MessageContent findByTypeAndUserLevel(
        Long typeId, Integer userLevel, Integer childLevel) {
        return messageContentQueryRepository.findByTypeAndUserLevel(typeId, userLevel, childLevel);
    }

    @Transactional
    public MessageContent generateAiContent(Channel channel, AiGenerateRequestDto requestDto) {

        // 1. MessageType 조회 (없으면 에러)
        MessageType messageType = messageTypeJpaRepository
            .findByChannelAndDeliveryTime(channel, requestDto.deliveryDate())
            .orElseThrow(() -> new MessageException(MESSAGE_TYPE_NOT_FOUND));

        // 2. 동일한 레벨의 MessageContent가 이미 존재하는지 확인
        Optional<MessageContent> existingContent = messageContentJpaRepository
            .findByMessageTypeAndChildLevelAndUserLevel(
                messageType, requestDto.childLevel(), requestDto.userLevel());

        // regenerate=false이고 기존 컨텐츠가 있으면 기존 컨텐츠 반환
        if (existingContent.isPresent() && !requestDto.regenerate()) {
            return existingContent.get();
        }

        // 3. 메시지 프롬프트를 갖고 온다
        MessagePrompt messagePrompt = messagePromptQueryRepository.findSelectedPromptByChannelAndType(
            channel, PromptType.BASIC);

        // 4. OpenAI 프롬프트 생성 및 API 호출
        String generatedContent = openAiService.generateData(messagePrompt, messageType,
            requestDto.userLevel(), requestDto.childLevel());

        // 5. MessageContent 생성 또는 업데이트
        if (existingContent.isPresent()) {
            // regenerate=true이고 기존 컨텐츠가 있으면 업데이트
            MessageContent existingMessageContent = existingContent.get();
            existingMessageContent.updateContent(generatedContent,
                existingMessageContent.getMommyVoca());
            return messageContentJpaRepository.save(existingMessageContent);
        } else {
            // 새 컨텐츠 생성
            MessageContent messageContent = MessageContent.createByAi(
                messageType,
                generatedContent,
                requestDto.childLevel(),
                requestDto.userLevel()
            );
            return messageContentJpaRepository.save(messageContent);
        }
    }

    @Transactional
    public Long upsertMessageContent(Long channelId, UpsertMessageContentRequestDto requestDto) {

        // MessageType 조회 (없으면 에러)
        MessageType messageType = messageTypeJpaRepository
            .findById(requestDto.messageTypeId())
            .orElseThrow(() -> new MessageException(MESSAGE_TYPE_NOT_FOUND));

        // 채널 권한 검증 (content의 messageType의 channel이 요청한 channelId와 일치하는지)
        if (!messageType.getChannel().getId().equals(channelId)) {
            throw new MessageException(MESSAGE_CONTENT_ACCESS_DENIED);
        }

        // 동일한 레벨의 MessageContent가 이미 존재하는지 확인
        Optional<MessageContent> existingContent = messageContentJpaRepository
            .findByMessageTypeAndChildLevelAndUserLevel(
                messageType, requestDto.childLevel(), requestDto.userLevel());

        if (existingContent.isPresent()) {
            MessageContent messageContent = existingContent.get();
            messageContent.updateContent(requestDto.content(), requestDto.mommyVoca());
            messageContentJpaRepository.save(messageContent);

            return existingContent.get().getId();
        } else {
            // 새 컨텐츠 생성
            MessageContent messageContent = MessageContent.ofWithMockUrlsForUpsert(
                messageType,
                requestDto.content(),
                requestDto.mommyVoca(),
                requestDto.childLevel(),
                requestDto.userLevel()
            );
            MessageContent newContent = messageContentJpaRepository.save(messageContent);
            return newContent.getId();
        }
    }

    @Transactional
    public ElevenLabsMedia updateContentAudio(
        Channel channelInfo, Long contentId, ContentAudioRequestDto requestDto) {

        // 1. MessageContent 조회
        MessageContent messageContent = messageContentJpaRepository.findById(contentId)
            .orElseThrow(() -> new MessageException(MESSAGE_CONTENT_NOT_FOUND));

        // 2. 기존 오디오 삭제 처리
        deleteOldAudioIfExists(messageContent, requestDto.audioRole());

        // 3. 새 오디오 생성
        ElevenLabsMedia elevenLabsMedia = elevenLabsService.generateAudio(
            requestDto.toElevenLabsRequest(), contentId, requestDto.modelId());

        // 4. MessageContent 업데이트
        switch (requestDto.audioRole()) {
            case MOMMY -> messageContent.updateButtonOne(elevenLabsMedia);
            case CHILD -> messageContent.updateButtonTwo(elevenLabsMedia);
            default -> {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "지원하지 않는 Audio Role");
            }
        }

        return elevenLabsMedia;
    }

    /**
     * 기존 오디오 삭제 처리
     */
    private void deleteOldAudioIfExists(MessageContent messageContent, AudioRole audioRole) {
        ElevenLabsMedia oldMedia = switch (audioRole) {
            case MOMMY -> messageContent.getHeaderOneLink();
            case CHILD -> messageContent.getHeaderTwoLink();
        };

        if (oldMedia == null) {
            log.info("기존 {} 오디오 없음, 삭제 스킵", audioRole);
            return;
        }

        log.info("기존 {} 오디오 삭제 시작 - ID: {}, URL: {}",
            audioRole, oldMedia.getId(), oldMedia.getFileUrl());

        // 1. R2에서 파일 삭제 (실패해도 계속 진행)
        try {
            String fileKey = oldMedia.extractFileKey();
            if (fileKey != null) {
                cloudflareService.deleteFile(fileKey);
                log.info("R2 파일 삭제 성공: {}", fileKey);
            }
        } catch (Exception e) {
            log.warn("R2 파일 삭제 실패 (작업 계속 진행): {}", e.getMessage());
            // 실패해도 계속 진행
        }

        // 2. ElevenLabsMedia 논리 삭제
        try {
            oldMedia.markAsDeleted();
            elevenLabsMediaRepository.save(oldMedia);
            log.info("ElevenLabsMedia 논리 삭제 완료 - ID: {}", oldMedia.getId());
        } catch (Exception e) {
            log.error("ElevenLabsMedia 논리 삭제 실패: {}", e.getMessage(), e);
            // 이것도 실패해도 계속 진행
        }
    }

    @Transactional
    public void approveMessageContent(Long channelId, Long contentId) {

        // 1. MessageContent 조회
        MessageContent messageContent = messageContentJpaRepository.findById(contentId)
            .orElseThrow(() -> new MessageException(MESSAGE_CONTENT_NOT_FOUND));

        // 2. 채널 권한 검증 (content의 messageType의 channel이 요청한 channelId와 일치하는지)
        if (!messageContent.getMessageType().getChannel().getId().equals(channelId)) {
            throw new MessageException(MESSAGE_CONTENT_ACCESS_DENIED);
        }

        // 승인 여부 확인
        if (!messageContent.checkApprovedCondition()) {
            throw new MessageException(NEED_MORE_DATE_FOR_APPROVED);
        }

        // 3. 승인 처리 (이미 승인된 경우 DB 업데이트 안함)
        boolean needsUpdate = messageContent.approve();

        // 4. 필요한 경우에만 저장 (성능 최적화)
        if (needsUpdate) {
            messageContentJpaRepository.save(messageContent);
        }
    }

    public ContentStatusResponseDto getContentStatus(Long channelId, LocalDate date) {

        // 1. 해당 날짜의 MessageType 조회
        Optional<MessageType> messageType = messageTypeJpaRepository
            .findByChannelIdAndDeliveryTime(channelId, date);

        // 2. MessageType이 없으면 모두 0으로 반환
        if (messageType.isEmpty()) {
            return ContentStatusResponseDto.of(0, 0);
        }

        // 3. MessageContent 개수 집계
        int generatedCount = messageContentJpaRepository
            .countByMessageType(messageType.get());

        int approvedCount = messageContentJpaRepository
            .countByMessageTypeAndApprovedTrue(messageType.get());

        return ContentStatusResponseDto.of(generatedCount, approvedCount);
    }


    public Map<String, MessageContent> groupMessageContentByLevel(MessageType messageType) {
        return messageType.getMessageContentList()
            .stream()
            .collect(Collectors.toMap(
                MessageContent::createKeyPropertyForMessageContent, // key: "1_2"
                mt -> mt
            ));
    }

    public Map<String, String> groupMessageTextByLevel(MessageType messageType) {
        return messageType.getMessageContentList()
            .stream()
            .collect(Collectors.toMap(
                MessageContent::createKeyPropertyForMessageContent, // key: "1_2"
                MessageContent::getContent
            ));
    }

    public Map<String, Boolean> groupMessageApprovedByLevel(MessageType messageType) {
        return messageType.getMessageContentList()
            .stream()
            .collect(Collectors.toMap(
                MessageContent::createKeyPropertyForMessageContent, // key: "1_2"
                MessageContent::getApproved
            ));
    }

    @Override
    public MessageContentResponseDto findContentForUser(Long channelId, Long userId,
        Long messageLogDetailId) {

        // 1. MessageLogDetail 조회 (유저가 받은 메시지인지 확인, Entitlement JOIN 포함)
        MessageLogDetail messageLogDetail = messageLogDetailQueryRepository
            .findByChannelAndUserAndContent(channelId, userId, messageLogDetailId);

        // 2. MessageLogDetail이 없으면 404 에러
        if (messageLogDetail == null) {
            throw new MessageException(MESSAGE_CONTENT_NOT_FOUND);
        }

        // 3. MessageContent 조회
        MessageContent messageContent = messageLogDetail.getMessageContent();

        // 4. MessageLog의 entitlement.type이 MOMMYVOCA인지 확인
        boolean isMommyVoca = messageLogDetail.getMessageLog().getEntitlement() != null
            && messageLogDetail.getMessageLog().getEntitlement().getType()
            == EntitlementType.MOMMYVOCA;

        // 5. MOMMYVOCA가 아니면 mommyVoca를 null로 설정한 DTO 반환
        if (!isMommyVoca) {
            return MessageContentResponseDto.ofWithoutMommyVoca(messageContent);
        }

        // 6. MOMMYVOCA면 전체 데이터 반환
        return MessageContentResponseDto.of(messageContent);
    }

}