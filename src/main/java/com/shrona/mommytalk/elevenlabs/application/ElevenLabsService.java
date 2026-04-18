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
    ElevenLabsMedia generateAudio(ElevenLabsRequest request, Long messageContentId, String voiceId);

    /**
     * 오디오 생성 및 저장. existingMedia가 있으면 R2 파일 교체 + row 업데이트, 없으면 새 row 생성.
     *
     * @param request          ElevenLabs 요청 DTO
     * @param messageContentId 메시지 콘텐츠 ID (파일명에 사용)
     * @param voiceId          사용할 Voice ID
     * @param existingMedia    기존 미디어 (null이면 신규 생성)
     * @return 저장된 ElevenLabsMedia
     */
    ElevenLabsMedia saveAudio(ElevenLabsRequest request, Long messageContentId, String voiceId,
        ElevenLabsMedia existingMedia);
}
