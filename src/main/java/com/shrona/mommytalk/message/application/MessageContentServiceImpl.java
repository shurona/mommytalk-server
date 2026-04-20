package com.shrona.mommytalk.message.application;

import static com.shrona.mommytalk.message.common.exception.MessageErrorCode.BAD_REQUEST;
import static com.shrona.mommytalk.message.common.exception.MessageErrorCode.CHILD_LEVEL_1_AUDIO_NOT_SUPPORTED;
import static com.shrona.mommytalk.message.common.exception.MessageErrorCode.MESSAGE_CONTENT_ACCESS_DENIED;
import static com.shrona.mommytalk.message.common.exception.MessageErrorCode.MESSAGE_CONTENT_NOT_FOUND;
import static com.shrona.mommytalk.message.common.exception.MessageErrorCode.MESSAGE_TYPE_NOT_FOUND;
import static com.shrona.mommytalk.message.common.exception.MessageErrorCode.NEED_MORE_DATE_FOR_APPROVED;
import static com.shrona.mommytalk.message.common.exception.MessageErrorCode.SOURCE_AUDIO_NOT_GENERATED;
import static com.shrona.mommytalk.message.common.exception.MessageErrorCode.SOURCE_CONTENT_MISMATCH;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.cloudflare.application.CloudflareService;
import com.shrona.mommytalk.elevenlabs.application.ElevenLabsService;
import com.shrona.mommytalk.elevenlabs.domain.ElevenLabsMedia;
import com.shrona.mommytalk.elevenlabs.infrastructure.reposiotry.ElevenLabsMediaRepository;
import com.shrona.mommytalk.elevenlabs.infrastructure.sender.dto.ElevenLabsRequest;
import com.shrona.mommytalk.elevenlabs.infrastructure.sender.dto.ElevenLabsRequest.VoiceSettings;
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
import com.shrona.mommytalk.message.common.exception.BatchValidationException;
import com.shrona.mommytalk.message.presentation.dtos.request.AiGenerateRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.ApplyLevelAudioRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.BatchAudioRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.BulkImportMessageRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.ContentAudioRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.UpdateAudioTextsRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.UpdateAudioTextsRequestDto.AudioTextItem;
import com.shrona.mommytalk.message.presentation.dtos.request.UpdateMommyVocaRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.UpsertMessageContentRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.response.AudioTextsResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.AudioTextsResponseDto.AudioTextItemDto;
import com.shrona.mommytalk.message.presentation.dtos.response.ApplyLevelAudioResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.ApplyLevelAudioResponseDto.ApplyLevelResultDto;
import com.shrona.mommytalk.message.presentation.dtos.response.BatchAudioResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.BatchAudioResponseDto.BatchAudioResultDto;
import com.shrona.mommytalk.message.presentation.dtos.response.ContentMommyVocaUpdateResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.ContentStatusResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.MessageContentResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.MommyVocaUpdateResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.UpdateAudioTextsResponseDto;
import com.shrona.mommytalk.openai.application.OpenAiServiceImpl;
import com.shrona.mommytalk.openai.domain.MessagePrompt;
import com.shrona.mommytalk.openai.domain.type.PromptType;
import com.shrona.mommytalk.openai.infrastructure.repository.query.MessagePromptQueryRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
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
    private final Environment environment;
    private final MessageTypeService messageTypeService;

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
            messageContent.updateContent(requestDto.content());
            messageContentJpaRepository.save(messageContent);

            return existingContent.get().getId();
        } else {
            // 새 컨텐츠 생성
            MessageContent messageContent = MessageContent.ofWithMockUrlsForUpsert(
                messageType,
                requestDto.content(),
                null,
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

        // 2. 기존 미디어 row 가져오기 (재사용)
        ElevenLabsMedia existingMedia = switch (requestDto.audioRole()) {
            case MOMMY -> messageContent.getHeaderOneLink();
            case CHILD -> messageContent.getHeaderTwoLink();
        };

        // 3. 오디오 생성 (existingMedia 있으면 R2 교체 + row 업데이트, 없으면 신규)
        ElevenLabsMedia elevenLabsMedia = elevenLabsService.saveAudio(
            requestDto.toElevenLabsRequest(), contentId, requestDto.modelId(), existingMedia);

        // 4. 신규 row일 때만 MessageContent 연결 업데이트
        if (existingMedia == null) {
            switch (requestDto.audioRole()) {
                case MOMMY -> messageContent.updateButtonOne(elevenLabsMedia);
                case CHILD -> messageContent.updateButtonTwo(elevenLabsMedia);
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "지원하지 않는 Audio Role");
            }
        }

        return elevenLabsMedia;
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

    private static final int REQUIRED_CONTENT_COUNT = 9;

    private static final List<String> ALL_LEVEL_KEYS = List.of(
        "1_1", "1_2", "1_3", "2_1", "2_2", "2_3", "3_1", "3_2", "3_3"
    );

    @Override
    public AudioTextsResponseDto getAudioTexts(Long channelId, Long messageTypeId) {
        MessageType messageType = messageTypeJpaRepository.findById(messageTypeId)
            .orElseThrow(() -> new MessageException(MESSAGE_TYPE_NOT_FOUND));

        if (!messageType.getChannel().getId().equals(channelId)) {
            throw new MessageException(MESSAGE_CONTENT_ACCESS_DENIED);
        }

        Map<String, MessageContent> contentByLevel = groupMessageContentByLevel(messageType);

        List<AudioTextItemDto> items = new ArrayList<>();
        for (int userLevel = 1; userLevel <= 3; userLevel++) {
            for (int childLevel = 1; childLevel <= 3; childLevel++) {
                String key = userLevel + "_" + childLevel;
                MessageContent content = contentByLevel.get(key);
                if (content == null) {
                    items.add(new AudioTextItemDto(null, userLevel, childLevel, false,
                        "", "", "", "", ""));
                } else {
                    String momAudioText = content.getHeaderOneLink() != null
                        ? content.getHeaderOneLink().getText() : "";
                    String childAudioText = content.getHeaderTwoLink() != null
                        ? content.getHeaderTwoLink().getText() : "";
                    String momAudioUrl = content.getHeaderOneLink() != null
                        && content.getHeaderOneLink().getFileUrl() != null
                        ? content.getHeaderOneLink().getFileUrl() : "";
                    String childAudioUrl = content.getHeaderTwoLink() != null
                        && content.getHeaderTwoLink().getFileUrl() != null
                        ? content.getHeaderTwoLink().getFileUrl() : "";
                    items.add(new AudioTextItemDto(
                        content.getId(), userLevel, childLevel, true,
                        content.getContent() != null ? content.getContent() : "",
                        momAudioText != null ? momAudioText : "",
                        childAudioText != null ? childAudioText : "",
                        momAudioUrl, childAudioUrl
                    ));
                }
            }
        }

        return new AudioTextsResponseDto(messageTypeId, items);
    }

    @Override
    @Transactional
    public UpdateAudioTextsResponseDto updateAudioTexts(Long channelId, Long messageTypeId,
        UpdateAudioTextsRequestDto req) {

        MessageType messageType = messageTypeJpaRepository.findById(messageTypeId)
            .orElseThrow(() -> new MessageException(MESSAGE_TYPE_NOT_FOUND));

        if (!messageType.getChannel().getId().equals(channelId)) {
            throw new MessageException(MESSAGE_CONTENT_ACCESS_DENIED);
        }

        List<MessageContent> allContents = messageContentJpaRepository.findByMessageType(
            messageType);

        if (allContents.size() < REQUIRED_CONTENT_COUNT) {
            List<String> existingKeys = allContents.stream()
                .map(MessageContent::createKeyPropertyForMessageContent)
                .toList();
            List<String> missingLevels = ALL_LEVEL_KEYS.stream()
                .filter(k -> !existingKeys.contains(k))
                .toList();
            throw new BatchValidationException(
                "All 9 contents must exist before updating audio texts.",
                Map.of("missingLevels", missingLevels));
        }

        Map<String, MessageContent> contentByLevel = allContents.stream()
            .collect(Collectors.toMap(MessageContent::createKeyPropertyForMessageContent, c -> c));

        // null 텍스트 검증
        for (AudioTextItem item : req.items()) {
            if (item.momAudioText() == null || item.childAudioText() == null) {
                throw new MessageException(BAD_REQUEST);
            }
        }

        int updatedCount = 0;
        for (AudioTextItem item : req.items()) {
            String key = item.userLevel() + "_" + item.childLevel();
            MessageContent content = contentByLevel.get(key);
            if (content == null) {
                continue;
            }

            // MOMMY
            if (content.getHeaderOneLink() != null) {
                content.getHeaderOneLink().updateText(item.momAudioText());
            } else {
                ElevenLabsMedia newMedia = elevenLabsMediaRepository.save(
                    ElevenLabsMedia.ofTextOnly(item.momAudioText()));
                content.updateButtonOne(newMedia);
            }

            // CHILD
            if (content.getHeaderTwoLink() != null) {
                content.getHeaderTwoLink().updateText(item.childAudioText());
            } else {
                ElevenLabsMedia newMedia = elevenLabsMediaRepository.save(
                    ElevenLabsMedia.ofTextOnly(item.childAudioText()));
                content.updateButtonTwo(newMedia);
            }

            updatedCount++;
        }

        return new UpdateAudioTextsResponseDto(messageTypeId, updatedCount, LocalDateTime.now());
    }

    @Override
    @Transactional
    public BatchAudioResponseDto batchCreateAudio(Long channelId, Long messageTypeId,
        BatchAudioRequestDto req) {

        MessageType messageType = messageTypeJpaRepository.findById(messageTypeId)
            .orElseThrow(() -> new MessageException(MESSAGE_TYPE_NOT_FOUND));

        if (!messageType.getChannel().getId().equals(channelId)) {
            throw new MessageException(MESSAGE_CONTENT_ACCESS_DENIED);
        }

        List<MessageContent> allContents = messageContentJpaRepository.findByMessageType(
            messageType);

        // 9개 content 존재 검증
        if (allContents.size() < REQUIRED_CONTENT_COUNT) {
            List<String> existingKeys = allContents.stream()
                .map(MessageContent::createKeyPropertyForMessageContent)
                .toList();
            List<String> missingLevels = ALL_LEVEL_KEYS.stream()
                .filter(k -> !existingKeys.contains(k))
                .toList();
            throw new BatchValidationException(
                "All 9 contents must exist before batch audio generation.",
                Map.of("missingLevels", missingLevels));
        }

        // 오디오 텍스트 존재 검증 (childLevel=1은 아이 음성 없음)
        List<Map<String, Object>> missingAudioText = new ArrayList<>();
        for (MessageContent content : allContents) {
            boolean momMissing = content.getHeaderOneLink() == null
                || content.getHeaderOneLink().getText() == null
                || content.getHeaderOneLink().getText().isBlank();

            if (momMissing) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("userLevel", content.getUserLevel());
                entry.put("childLevel", content.getChildLevel());
                entry.put("audioRole", "MOMMY");
                missingAudioText.add(entry);
            }

            if (content.getChildLevel() != null && content.getChildLevel() == 1) {
                continue;
            }

            boolean childMissing = content.getHeaderTwoLink() == null
                || content.getHeaderTwoLink().getText() == null
                || content.getHeaderTwoLink().getText().isBlank();

            if (childMissing) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("userLevel", content.getUserLevel());
                entry.put("childLevel", content.getChildLevel());
                entry.put("audioRole", "CHILD");
                missingAudioText.add(entry);
            }
        }

        if (!missingAudioText.isEmpty()) {
            throw new BatchValidationException(
                "Audio text is missing for one or more contents.",
                Map.of("missingAudioText", missingAudioText));
        }

        // 작업 데이터 사전 추출 (lazy loading은 main thread에서 처리)
        // childLevel=1은 아이 음성 생성 스킵
        List<AudioGenTask> tasks = new ArrayList<>();
        for (MessageContent content : allContents) {
            tasks.add(new AudioGenTask(
                content.getId(), content.getUserLevel(), content.getChildLevel(),
                "MOMMY", content.getHeaderOneLink(),
                req.mommy().modelId(), req.mommy().speed()));

            if (content.getChildLevel() != null && content.getChildLevel() == 1) {
                continue;
            }

            tasks.add(new AudioGenTask(
                content.getId(), content.getUserLevel(), content.getChildLevel(),
                "CHILD", content.getHeaderTwoLink(),
                req.child().modelId(), req.child().speed()));
        }

        // ElevenLabs 동시 요청 2개로 제한
        List<BatchAudioResultDto> results;
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<CompletableFuture<BatchAudioResultDto>> futures = tasks.stream()
                .map(task -> CompletableFuture.supplyAsync(
                    () -> processAudioTask(task), executor))
                .toList();
            results = futures.stream().map(CompletableFuture::join).toList();
        } finally {
            executor.shutdown();
        }

        int successCount = (int) results.stream().filter(BatchAudioResultDto::success).count();
        int failureCount = results.size() - successCount;

        return new BatchAudioResponseDto(successCount, failureCount, results);
    }

    private BatchAudioResultDto processAudioTask(AudioGenTask task) {
        try {
            ElevenLabsRequest request = new ElevenLabsRequest(
                task.existingMedia().getText(), null, null,
                Optional.of(new VoiceSettings(null, null, null, null, task.speed())));
            ElevenLabsMedia result = elevenLabsService.saveAudio(
                request, task.contentId(), task.voiceId(), task.existingMedia());
            return new BatchAudioResultDto(
                task.contentId(), task.userLevel(), task.childLevel(),
                task.role(), true, result.getFileUrl(), null);
        } catch (Exception e) {
            log.error("배치 오디오 생성 실패 - contentId={}, role={}: {}",
                task.contentId(), task.role(), e.getMessage());
            return new BatchAudioResultDto(
                task.contentId(), task.userLevel(), task.childLevel(),
                task.role(), false, null, e.getMessage());
        }
    }

    private record AudioGenTask(
        Long contentId, Integer userLevel, Integer childLevel,
        String role, ElevenLabsMedia existingMedia,
        String voiceId, Double speed
    ) {

    }

    @Override
    public String getMommyVocaForType(MessageType messageType) {
        return messageType.getMessageContentList().stream()
            .map(MessageContent::getMommyVoca)
            .filter(v -> v != null && !v.isBlank())
            .findFirst()
            .orElse(null);
    }

    @Override
    @Transactional
    public MommyVocaUpdateResponseDto updateMommyVocaForType(Long channelId, Long messageTypeId,
        UpdateMommyVocaRequestDto requestDto) {

        MessageType messageType = messageTypeJpaRepository.findById(messageTypeId)
            .orElseThrow(() -> new MessageException(MESSAGE_TYPE_NOT_FOUND));

        if (!messageType.getChannel().getId().equals(channelId)) {
            throw new MessageException(MESSAGE_CONTENT_ACCESS_DENIED);
        }

        List<MessageContent> contents = messageContentJpaRepository.findByMessageType(messageType);

        if (contents.size() < REQUIRED_CONTENT_COUNT) {
            throw new MessageException(NEED_MORE_DATE_FOR_APPROVED);
        }

        contents.forEach(c -> c.updateMommyVoca(requestDto.mommyVoca()));
        messageContentJpaRepository.saveAll(contents);

        return new MommyVocaUpdateResponseDto(messageTypeId, requestDto.mommyVoca(),
            messageType.getUpdatedAt());
    }

    @Override
    @Transactional
    public ContentMommyVocaUpdateResponseDto updateMommyVocaForContent(Long channelId,
        Long contentId, UpdateMommyVocaRequestDto requestDto) {

        MessageContent messageContent = messageContentJpaRepository.findById(contentId)
            .orElseThrow(() -> new MessageException(MESSAGE_CONTENT_NOT_FOUND));

        if (!messageContent.getMessageType().getChannel().getId().equals(channelId)) {
            throw new MessageException(MESSAGE_CONTENT_ACCESS_DENIED);
        }

        messageContent.updateMommyVoca(requestDto.mommyVoca());
        messageContentJpaRepository.save(messageContent);

        return new ContentMommyVocaUpdateResponseDto(contentId, requestDto.mommyVoca(),
            messageContent.getUpdatedAt());
    }

    @Override
    public MessageContentResponseDto findContentForUser(Long channelId, Long userId,
        Long messageLogDetailId) {

        // local 환경에서는 userId를 152L로 고정
        if (Arrays.asList(environment.getActiveProfiles()).contains("local")
            && messageLogDetailId.equals(152L)) {
            log.info("테스트 시 강제 조회 설정");
            userId = 102L;
        }

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

    @Override
    @Transactional
    public void bulkImportLegacyData(Channel channel, List<BulkImportMessageRequestDto> requests) {
        log.info("[레거시 데이터 임포트 시작] channelId={}, 데이터 개수={}", channel.getId(),
            requests.size());

        for (BulkImportMessageRequestDto request : requests) {
            if (StringUtils.isBlank(request.contents())) {
                continue;
            }
            try {
                // 1. MessageType 생성 또는 조회
                Optional<MessageType> existingType = messageTypeJpaRepository
                    .findByChannelAndDeliveryTime(channel, request.date());

                MessageType messageType;
                if (existingType.isPresent()) {
                    messageType = existingType.get();
                    log.info("기존 MessageType 사용 - date={}, theme={}", request.date(),
                        request.title());
                } else {
                    messageType = messageTypeService.createMessageType(
                        request.title(),  // theme
                        "",              // context (빈값)
                        request.date(),
                        channel
                    );
                    log.info("새 MessageType 생성 - date={}, theme={}", request.date(),
                        request.title());
                }

                // 2. MessageContent 생성 (userLevel=2, childLevel=2 고정)
                Optional<MessageContent> existingContent = messageContentJpaRepository
                    .findByMessageTypeAndChildLevelAndUserLevel(messageType, 2, 2);

                MessageContent messageContent;
                if (existingContent.isPresent()) {
                    messageContent = existingContent.get();
                    // 기존 컨텐츠 업데이트
                    messageContent.updateContent(request.contents(), request.link());
                    log.info("기존 MessageContent 업데이트 - contentId={}", messageContent.getId());
                } else {
                    // 새 컨텐츠 생성
                    messageContent = MessageContent.ofWithMockUrlsForUpsert(
                        messageType,
                        request.contents(),
                        request.link(),  // mommyVoca
                        2,  // childLevel
                        2   // userLevel
                    );
                    messageContentJpaRepository.save(messageContent);
                    log.info("새 MessageContent 생성 - contentId={}", messageContent.getId());
                }

                // 3. 승인 처리
                boolean approved = messageContent.approve();
                if (approved) {
                    log.info("MessageContent 승인 완료 - contentId={}", messageContent.getId());
                }

            } catch (Exception e) {
                log.error("[레거시 데이터 임포트 실패] date={}, title={}, error={}",
                    request.date(), request.title(), e.getMessage(), e);
                throw new MessageException(
                    com.shrona.mommytalk.message.common.exception.MessageErrorCode.BAD_REQUEST);
            }
        }

        log.info("[레거시 데이터 임포트 완료] 총 {}건 처리", requests.size());
    }

    @Override
    @Transactional
    public int uploadLegacyAudio(Channel channel, int year, int month) {
        log.info("[레거시 MP3 업로드 시작] channelId={}, year={}, month={}",
            channel.getId(), year, month);

        // 1. CSV 파일 읽기
        String csvPath = "elevenlabs/mp3-mapping.csv";
        List<CsvRow> csvRows = parseCsv(csvPath, year, month);

        log.info("[CSV 파싱 완료] 필터링된 행 개수={}", csvRows.size());

        int totalUploaded = 0;
        int headerOneCount = 0;
        int headerTwoCount = 0;

        // 2. 각 CSV 행 처리
        for (CsvRow row : csvRows) {
            try {
                int uploaded = processLegacyAudioRow(row, channel);
                totalUploaded += uploaded;

                if (uploaded == 2) {
                    headerOneCount++;
                    headerTwoCount++;
                } else if (uploaded == 1) {
                    headerOneCount++;
                }
            } catch (Exception e) {
                log.error("[레거시 MP3 업로드 실패] 번호={}, 날짜={}, error={}",
                    row.number(), row.date(), e.getMessage(), e);
            }
        }

        log.info("[레거시 MP3 업로드 완료] 총 업로드={}, headerOne={}, headerTwo={}",
            totalUploaded, headerOneCount, headerTwoCount);

        return totalUploaded;
    }

    /**
     * CSV 파일 파싱 (년월 필터링)
     */
    private List<CsvRow> parseCsv(String csvPath, int year, int month) {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get(csvPath);
            List<String> lines = java.nio.file.Files.readAllLines(path);

            return lines.stream()
                .skip(1) // 헤더 제외
                .map(line -> {
                    String[] parts = line.split(",");
                    if (parts.length < 2) {
                        return null;
                    }

                    try {
                        int number = Integer.parseInt(parts[0].trim());
                        String dateStr = parts[1].trim(); // "2025.2.19" 형식
                        LocalDate date = parseDate(dateStr);

                        return new CsvRow(number, date);
                    } catch (Exception e) {
                        log.warn("[CSV 파싱 실패] line={}", line);
                        return null;
                    }
                })
                .filter(row -> row != null)
                .filter(row -> row.date().getYear() == year && row.date().getMonthValue() == month)
                .toList();

        } catch (Exception e) {
            log.error("[CSV 파일 읽기 실패] path={}, error={}", csvPath, e.getMessage(), e);
            throw new RuntimeException("CSV 파일 읽기 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 날짜 문자열 파싱 ("2025.2.19" → LocalDate)
     */
    private LocalDate parseDate(String dateStr) {
        String[] parts = dateStr.split("\\.");
        int year = Integer.parseInt(parts[0].trim());
        int month = Integer.parseInt(parts[1].trim());
        int day = Integer.parseInt(parts[2].trim());
        return LocalDate.of(year, month, day);
    }

    /**
     * 각 CSV 행 처리 (MP3 업로드 및 MessageContent 업데이트)
     */
    private int processLegacyAudioRow(CsvRow row, Channel channel) {
        log.info("[레거시 MP3 처리 시작] 번호={}, 날짜={}", row.number(), row.date());

        // 1. MessageType 조회
        Optional<MessageType> messageTypeOpt = messageTypeJpaRepository
            .findByChannelAndDeliveryTime(channel, row.date());

        if (messageTypeOpt.isEmpty()) {
            log.warn("[MessageType 없음 - 스킵] 날짜={}", row.date());
            return 0;
        }

        MessageType messageType = messageTypeOpt.get();

        // 2. MessageContent 조회 (level 2-2)
        Optional<MessageContent> contentOpt = messageContentJpaRepository
            .findByMessageTypeAndChildLevelAndUserLevel(messageType, 2, 2);

        if (contentOpt.isEmpty()) {
            log.warn("[MessageContent 없음 - 스킵] 날짜={}, messageTypeId={}",
                row.date(), messageType.getId());
            return 0;
        }

        MessageContent messageContent = contentOpt.get();
        int uploadCount = 0;

        // 3. {번호}.mp3 파일 처리 (headerOneLink)
        String mainMp3Path = String.format("elevenlabs/mp3/%d.mp3", row.number());
        java.io.File mainMp3File = new java.io.File(mainMp3Path);

        if (mainMp3File.exists()) {
            try {
                ElevenLabsMedia media = uploadMp3ToR2(mainMp3File, messageContent.getId());
                messageContent.updateButtonOne(media);
                uploadCount++;
                log.info("[headerOneLink 업데이트 완료] 번호={}, mediaId={}",
                    row.number(), media.getId());
            } catch (Exception e) {
                log.error("[headerOneLink 업로드 실패] 번호={}, error={}",
                    row.number(), e.getMessage(), e);
            }
        } else {
            log.debug("[{}.mp3 파일 없음] 번호={}", row.number());
        }

        // 4. {번호}-1.mp3 파일 처리 (headerTwoLink)
        String subMp3Path = String.format("elevenlabs/mp3/%d-1.mp3", row.number());
        java.io.File subMp3File = new java.io.File(subMp3Path);

        if (subMp3File.exists()) {
            try {
                ElevenLabsMedia media = uploadMp3ToR2(subMp3File, messageContent.getId());
                messageContent.updateButtonTwo(media);
                uploadCount++;
                log.info("[headerTwoLink 업데이트 완료] 번호={}, mediaId={}",
                    row.number(), media.getId());
            } catch (Exception e) {
                log.error("[headerTwoLink 업로드 실패] 번호={}, error={}",
                    row.number(), e.getMessage(), e);
            }
        } else {
            log.debug("[{}-1.mp3 파일 없음] 번호={}", row.number());
        }

        return uploadCount;
    }

    /**
     * MP3 파일을 R2에 업로드하고 ElevenLabsMedia 생성
     */
    private ElevenLabsMedia uploadMp3ToR2(java.io.File mp3File, Long messageContentId)
        throws java.io.IOException {
        // 1. 파일을 byte[]로 읽기
        byte[] audioBytes = java.nio.file.Files.readAllBytes(mp3File.toPath());

        // 2. 파일명 생성 (messageContent_{id}_{timestamp}.mp3)
        String fileName = String.format("messageContent_%d_%d.mp3",
            messageContentId, System.currentTimeMillis());

        // 3. R2 업로드
        String publicUrl = cloudflareService.uploadAudioBytes(audioBytes, fileName);

        // 4. ElevenLabsMedia 생성 (text=null)
        ElevenLabsMedia media = ElevenLabsMedia.of(null, publicUrl, fileName, audioBytes.length);
        elevenLabsMediaRepository.save(media);

        log.info("[R2 업로드 완료] fileName={}, url={}, size={}",
            fileName, publicUrl, audioBytes.length);

        return media;
    }

    /**
     * CSV 행 데이터 record
     */
    private record CsvRow(int number, LocalDate date) {

    }

    @Override
    @Transactional
    public ApplyLevelAudioResponseDto applyLevelAudio(Long channelId, Long messageTypeId,
        ApplyLevelAudioRequestDto requestDto) {

        if (requestDto.audioRole() == AudioRole.CHILD && requestDto.targetLevel() == 1) {
            throw new MessageException(CHILD_LEVEL_1_AUDIO_NOT_SUPPORTED);
        }

        MessageType messageType = messageTypeJpaRepository.findById(messageTypeId)
            .orElseThrow(() -> new MessageException(MESSAGE_TYPE_NOT_FOUND));

        if (!messageType.getChannel().getId().equals(channelId)) {
            throw new MessageException(MESSAGE_CONTENT_ACCESS_DENIED);
        }

        List<MessageContent> allContents = messageContentJpaRepository.findByMessageType(
            messageType);
        Map<String, MessageContent> contentByLevel = allContents.stream()
            .collect(Collectors.toMap(MessageContent::createKeyPropertyForMessageContent, c -> c));

        // source content 검증
        String sourceKey =
            requestDto.source().userLevel() + "_" + requestDto.source().childLevel();
        MessageContent sourceContent = contentByLevel.get(sourceKey);
        if (sourceContent == null || !sourceContent.getId().equals(requestDto.sourceContentId())) {
            throw new MessageException(SOURCE_CONTENT_MISMATCH);
        }

        ElevenLabsMedia sourceMedia = switch (requestDto.audioRole()) {
            case MOMMY -> sourceContent.getHeaderOneLink();
            case CHILD -> sourceContent.getHeaderTwoLink();
        };
        if (sourceMedia == null || sourceMedia.getFileUrl() == null) {
            throw new MessageException(SOURCE_AUDIO_NOT_GENERATED);
        }

        // 대상 3개 content 선택
        List<MessageContent> targets = switch (requestDto.audioRole()) {
            case MOMMY -> allContents.stream()
                .filter(c -> c.getUserLevel().equals(requestDto.targetLevel()))
                .toList();
            case CHILD -> allContents.stream()
                .filter(c -> c.getChildLevel().equals(requestDto.targetLevel()))
                .toList();
        };

        if (targets.size() < 3) {
            List<String> missingLevels = new ArrayList<>();
            if (requestDto.audioRole() == AudioRole.MOMMY) {
                for (int childLevel = 1; childLevel <= 3; childLevel++) {
                    String key = requestDto.targetLevel() + "_" + childLevel;
                    if (!contentByLevel.containsKey(key)) {
                        missingLevels.add(key);
                    }
                }
            } else {
                for (int userLevel = 1; userLevel <= 3; userLevel++) {
                    String key = userLevel + "_" + requestDto.targetLevel();
                    if (!contentByLevel.containsKey(key)) {
                        missingLevels.add(key);
                    }
                }
            }
            throw new BatchValidationException(
                "All same-level contents must exist before applying audio.",
                Map.of("missingLevels", missingLevels));
        }

        // source content 텍스트 업데이트 (파일은 이미 생성됨)
        sourceMedia.updateText(requestDto.audioText());

        // 파일 복사 방식으로 각 target에 적용 (source 제외)
        String sourceFileKey = sourceMedia.extractFileKey();
        List<ApplyLevelResultDto> results = new ArrayList<>();
        String audioText = requestDto.audioText();
        for (MessageContent target : targets) {
            // source content는 텍스트만 업데이트했으므로 결과만 추가
            if (target.getId().equals(sourceContent.getId())) {
                results.add(new ApplyLevelResultDto(
                    target.getId(), target.getUserLevel(), target.getChildLevel(),
                    true, sourceMedia.getFileUrl(), null));
                continue;
            }

            try {
                ElevenLabsMedia targetMedia = switch (requestDto.audioRole()) {
                    case MOMMY -> target.getHeaderOneLink();
                    case CHILD -> target.getHeaderTwoLink();
                };

                // 기존 R2 파일 삭제
                if (targetMedia != null && targetMedia.getFileUrl() != null) {
                    String oldKey = targetMedia.extractFileKey();
                    if (oldKey != null) {
                        try {
                            cloudflareService.deleteFile(oldKey);
                        } catch (Exception e) {
                            log.warn("기존 R2 파일 삭제 실패 (계속 진행): {}", e.getMessage());
                        }
                    }
                }

                // source 파일을 target용 새 키로 복사
                String destFileName = String.format("messageContent_%d_%d.mp3",
                    target.getId(), System.currentTimeMillis());
                String newFileUrl = cloudflareService.copyAudioFile(sourceFileKey, destFileName);

                if (targetMedia != null) {
                    targetMedia.updateText(audioText);
                    targetMedia.updateAudio(newFileUrl, destFileName, sourceMedia.getFileSize());
                } else {
                    ElevenLabsMedia newMedia = elevenLabsMediaRepository.save(
                        ElevenLabsMedia.of(audioText, newFileUrl, destFileName,
                            sourceMedia.getFileSize()));
                    switch (requestDto.audioRole()) {
                        case MOMMY -> target.updateButtonOne(newMedia);
                        case CHILD -> target.updateButtonTwo(newMedia);
                    }
                }
                results.add(new ApplyLevelResultDto(
                    target.getId(), target.getUserLevel(), target.getChildLevel(),
                    true, newFileUrl, null));
            } catch (Exception e) {
                log.error("레벨 적용 실패 - contentId={}: {}", target.getId(), e.getMessage());
                results.add(new ApplyLevelResultDto(
                    target.getId(), target.getUserLevel(), target.getChildLevel(),
                    false, null, e.getMessage()));
            }
        }

        int successCount = (int) results.stream().filter(ApplyLevelResultDto::success).count();
        int failureCount = results.size() - successCount;

        return new ApplyLevelAudioResponseDto(messageTypeId, requestDto.audioRole().name(),
            requestDto.targetLevel(), successCount, failureCount, results);
    }

}
