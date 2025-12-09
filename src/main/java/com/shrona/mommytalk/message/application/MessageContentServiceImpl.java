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
import com.shrona.mommytalk.message.presentation.dtos.request.BulkImportMessageRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.ContentAudioRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.UpsertMessageContentRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.response.ContentStatusResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.MessageContentResponseDto;
import com.shrona.mommytalk.openai.application.OpenAiServiceImpl;
import com.shrona.mommytalk.openai.domain.MessagePrompt;
import com.shrona.mommytalk.openai.domain.type.PromptType;
import com.shrona.mommytalk.openai.infrastructure.repository.query.MessagePromptQueryRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

}
