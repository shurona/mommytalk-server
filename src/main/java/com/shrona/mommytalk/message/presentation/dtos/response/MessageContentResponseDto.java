package com.shrona.mommytalk.message.presentation.dtos.response;

import static lombok.AccessLevel.PRIVATE;

import com.shrona.mommytalk.message.domain.MessageContent;
import java.time.format.DateTimeFormatter;
import lombok.Builder;

@Builder(access = PRIVATE)
public record MessageContentResponseDto(
    Long id,
    String theme,
    String context,
    String deliveryDate,
    Integer childLevel,
    Integer momLevel,
    String messageText,
    String momAudioUrl,
    String momAudioText,
    String childAudioUrl,
    String childAudioText,
    String mommyVoca,
    String diaryUrl,
    Boolean status,
    String createdAt,
    String updatedAt
) {

    public static MessageContentResponseDto of(MessageContent content) {

        String momAudioUrl = "";
        String momAudioText = "";

        String childAudioUrl = "";
        String childAudioText = "";

        if (content.getHeaderOneLink() != null) {
            momAudioUrl = content.getHeaderOneLink().getFileUrl();
            momAudioText = content.getHeaderOneLink().getText();
        }

        if (content.getHeaderTwoLink() != null) {
            childAudioUrl = content.getHeaderTwoLink().getFileUrl();
            childAudioText = content.getHeaderTwoLink().getText();
        }

        return MessageContentResponseDto.builder()
            .id(content.getId())
            .theme(content.getMessageType().getTheme())
            .context(content.getMessageType().getContext())
            .deliveryDate(content.getMessageType().getDeliveryTime().toString())
            .childLevel(content.getChildLevel())
            .momLevel(content.getUserLevel())
            .messageText(content.getContent())
            .momAudioText(momAudioText)
            .momAudioUrl(momAudioUrl)
            .childAudioText(childAudioText)
            .childAudioUrl(childAudioUrl)
            .mommyVoca(content.getMommyVoca())
            .diaryUrl(content.getDiaryUrl())
            .status(content.getApproved())
            .createdAt(content.getCreatedAt() != null ? content.getCreatedAt().plusHours(9)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")) : null)
            .updatedAt(content.getUpdatedAt() != null ? content.getUpdatedAt().plusHours(9)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")) : null)
            .build();
    }

    /**
     * MOMMYVOCA가 아닌 경우 mommyVoca를 null로 설정한 DTO 생성
     */
    public static MessageContentResponseDto ofWithoutMommyVoca(MessageContent content) {

        String momAudioUrl = "";
        String momAudioText = "";

        String childAudioUrl = "";
        String childAudioText = "";

        if (content.getHeaderOneLink() != null) {
            momAudioUrl = content.getHeaderOneLink().getFileUrl();
            momAudioText = content.getHeaderOneLink().getText();
        }

        if (content.getHeaderTwoLink() != null) {
            childAudioUrl = content.getHeaderTwoLink().getFileUrl();
            childAudioText = content.getHeaderTwoLink().getText();
        }

        return MessageContentResponseDto.builder()
            .id(content.getId())
            .theme(content.getMessageType().getTheme())
            .context(content.getMessageType().getContext())
            .deliveryDate(content.getMessageType().getDeliveryTime().toString())
            .childLevel(content.getChildLevel())
            .momLevel(content.getUserLevel())
            .messageText(content.getContent())
            .momAudioText(momAudioText)
            .momAudioUrl(momAudioUrl)
            .childAudioText(childAudioText)
            .childAudioUrl(childAudioUrl)
            .mommyVoca(null)  // MOMMYVOCA가 아니면 null
            .diaryUrl(content.getDiaryUrl())
            .status(content.getApproved())
            .createdAt(content.getCreatedAt() != null ? content.getCreatedAt().plusHours(9)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")) : null)
            .updatedAt(content.getUpdatedAt() != null ? content.getUpdatedAt().plusHours(9)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")) : null)
            .build();
    }

}