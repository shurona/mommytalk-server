package com.shrona.mommytalk.elevenlabs.application;

import com.shrona.mommytalk.elevenlabs.domain.ElevenLabsMedia;
import com.shrona.mommytalk.elevenlabs.infrastructure.sender.dto.ElevenLabsRequest;

/**
 * ElevenLabs TTS 서비스 인터페이스
 */
public interface ElevenLabsService {

    /**
     * 텍스트를 음성으로 변환하고 파일로 저장
     *
     * @param text             변환할 텍스트
     * @param messageContentId 메시지 콘텐츠 ID (파일명에 사용)
     * @return 저장된 파일 경로
     */
    String generateAudio(String text, Long messageContentId);

    /**
     * 텍스트를 음성으로 변환하고 파일로 저장 (옵션 포함)
     *
     * @param request          ElevenLabs 요청 DTO
     * @param messageContentId 메시지 콘텐츠 ID (파일명에 사용)
     * @return 저장된 파일 경로
     */
    ElevenLabsMedia generateAudio(ElevenLabsRequest request, Long messageContentId);
}
