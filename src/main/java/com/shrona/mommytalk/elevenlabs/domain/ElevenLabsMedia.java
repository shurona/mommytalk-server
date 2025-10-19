package com.shrona.mommytalk.elevenlabs.domain;


import com.shrona.mommytalk.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "elevenlabs_media")
public class ElevenLabsMedia extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String text;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_size")
    private Integer fileSize;

    public static ElevenLabsMedia of(
        String text, String fileUrl, String fileName, Integer fileSize) {

        ElevenLabsMedia elevenLabsMedia = new ElevenLabsMedia();
        elevenLabsMedia.text = text;
        elevenLabsMedia.fileUrl = fileUrl;
        elevenLabsMedia.fileName = fileName;
        elevenLabsMedia.fileSize = fileSize;

        return elevenLabsMedia;
    }

    /**
     * 논리 삭제 처리
     */
    public void markAsDeleted() {
        this.isDeleted = true;
    }

    /**
     * R2 파일 키 추출 (삭제용)
     * fileUrl에서 경로 추출: https://domain.com/audio/2025/01/file.mp3 → audio/2025/01/file.mp3
     */
    public String extractFileKey() {
        if (this.fileUrl == null) {
            return null;
        }
        int index = fileUrl.indexOf("/audio/");
        return index >= 0 ? fileUrl.substring(index + 1) : null;
    }

}
