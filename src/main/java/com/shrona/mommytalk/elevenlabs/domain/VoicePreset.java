package com.shrona.mommytalk.elevenlabs.domain;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.common.entity.BaseEntity;
import com.shrona.mommytalk.elevenlabs.domain.type.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

/**
 * 채널별 ElevenLabs 음성 프리셋 (관리용 카탈로그)
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction(BaseEntity.DEFAULT_CONDITION)
@Table(name = "voice_preset")
public class VoicePreset extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @Column
    private String name;

    @Enumerated(EnumType.STRING)
    @Column
    private Gender gender;

    @Column(name = "voice_id")
    private String voiceId;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public static VoicePreset of(
        Channel channel, String name, Gender gender, String voiceId, int sortOrder) {
        VoicePreset preset = new VoicePreset();
        preset.channel = channel;
        preset.name = name;
        preset.gender = gender;
        preset.voiceId = voiceId;
        preset.sortOrder = sortOrder;
        preset.isActive = true;
        return preset;
    }

    public void update(String name, Gender gender, String voiceId) {
        this.name = name;
        this.gender = gender;
        this.voiceId = voiceId;
    }

    public void updateSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void updateActive(boolean active) {
        this.isActive = active;
    }

    /**
     * 논리 삭제 처리
     */
    public void markAsDeleted() {
        this.isDeleted = true;
    }

}
