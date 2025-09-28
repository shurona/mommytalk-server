package com.shrona.mommytalk.message.presentation.dtos.response;

import static lombok.AccessLevel.PRIVATE;

import com.shrona.mommytalk.message.domain.MessageContent;
import java.time.format.DateTimeFormatter;
import lombok.Builder;

@Builder(access = PRIVATE)
public record MessageContentResponseDto(
    Long id,
    String theme,
    String deliveryDate,
    Integer childLevel,
    Integer momLevel,
    String language,
    String messageText,
    String momAudioUrl,
    String childAudioUrl,
    String vocaUrl,
    String diaryUrl,
    String status,
    String createdAt,
    String updatedAt
) {

    public static MessageContentResponseDto of(MessageContent content, String language) {
        return MessageContentResponseDto.builder()
            .id(content.getId())
            .theme(content.getMessageType().getTheme())
            .deliveryDate(content.getMessageType().getDeliveryTime().toString())
            .childLevel(content.getChildLevel())
            .momLevel(content.getUserLevel())
            .language(language)
            .messageText(content.getContent())
            .momAudioUrl(content.getHeaderOneLink() != null ? content.getHeaderOneLink() : "https://cdn.example.com/mock-mom-audio.mp3")
            .childAudioUrl(content.getHeaderTwoLink() != null ? content.getHeaderTwoLink() : "https://cdn.example.com/mock-child-audio.mp3")
            .vocaUrl(content.getMommyVoca())
            .diaryUrl(content.getDiaryUrl() != null ? content.getDiaryUrl() : "https://mamitalk.example.com/diary")
            .status(content.getApproved() != null && content.getApproved() ? "approved" : "generated")
            .createdAt(content.getCreatedAt() != null ? content.getCreatedAt().plusHours(9)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")) : null)
            .updatedAt(content.getUpdatedAt() != null ? content.getUpdatedAt().plusHours(9)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")) : null)
            .build();
    }
}