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

}
