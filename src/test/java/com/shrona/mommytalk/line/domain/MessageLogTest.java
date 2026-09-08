package com.shrona.mommytalk.line.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.channel.domain.ChannelPlatform;
import com.shrona.mommytalk.common.utils.DateTimeUtils;
import com.shrona.mommytalk.line.common.exception.LineException;
import com.shrona.mommytalk.message.domain.MessageLog;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MessageLogTest {

    @Test
    @DisplayName("메시지 내용 업데이트 성공 - 예약 시간 5분 이전일 경우")
    void updateMessageSuccessWhenBeforeFiveMinutesOfReservation() {
        // given
        LocalDateTime reserveTime = LocalDateTime.now().plusMinutes(10);
        MessageLog messageLog = MessageLog.messageLog(
            null, null, reserveTime, "원본 메시지");
        String newContent = "새로운 메시지";

        // when
        messageLog.updateMessage(newContent);

        // then
        Assertions.assertThat(messageLog.getGroupInfo()).isEqualTo(newContent);
    }

    @Test
    @DisplayName("메시지 내용 업데이트 실패 - 예약 시간 5분 이내일 경우")
    void updateMessageFailWhenWithinFiveMinutesOfReservation() {
        // given
        LocalDateTime reserveTime = LocalDateTime.now().plusMinutes(4);
        MessageLog messageLog = MessageLog.messageLog(
            null, null, reserveTime, "원본 메시지");
        String newContent = "새로운 메시지";

        // when, then
        Assertions.assertThatThrownBy(() -> messageLog.updateMessage(newContent))
            .isInstanceOf(LineException.class);

        // 메시지 내용이 변경되지 않았는지 확인
        assertEquals("원본 메시지", messageLog.getGroupInfo());
    }


    @Test
    @DisplayName("카카오 로그는 reserveTime이 지났어도 발송일(KST) 당일이면 상세 추가 가능")
    void canAddNewDetails_카카오_당일_reserveTime지남_가능() {
        // given: 오늘(KST) 0시를 UTC로 변환 → 항상 현재보다 과거이면서 발송일은 오늘
        LocalDateTime todayStartUtc = LocalDate.now(DateTimeUtils.KST)
            .atStartOfDay(DateTimeUtils.KST)
            .withZoneSameInstant(ZoneOffset.UTC)
            .toLocalDateTime();
        MessageLog messageLog = MessageLog.messageLog(
            channelOf(ChannelPlatform.KAKAO), null, todayStartUtc, "test");

        // when, then
        Assertions.assertThat(messageLog.canAddNewDetails()).isTrue();
    }

    @Test
    @DisplayName("카카오 로그는 발송일(KST)이 지나면 상세 추가 불가")
    void canAddNewDetails_카카오_발송일지남_불가() {
        // given
        MessageLog messageLog = MessageLog.messageLog(
            channelOf(ChannelPlatform.KAKAO), null, LocalDateTime.now().minusDays(1), "test");

        // when, then
        Assertions.assertThat(messageLog.canAddNewDetails()).isFalse();
    }

    @Test
    @DisplayName("라인 로그는 reserveTime이 지나면 상세 추가 불가")
    void canAddNewDetails_라인_reserveTime지남_불가() {
        // given
        MessageLog messageLog = MessageLog.messageLog(
            channelOf(ChannelPlatform.LINE), null, LocalDateTime.now().minusMinutes(1), "test");

        // when, then
        Assertions.assertThat(messageLog.canAddNewDetails()).isFalse();
    }

    @Test
    @DisplayName("라인 로그는 reserveTime 전이면 상세 추가 가능")
    void canAddNewDetails_라인_reserveTime전_가능() {
        // given
        MessageLog messageLog = MessageLog.messageLog(
            channelOf(ChannelPlatform.LINE), null, LocalDateTime.now().plusMinutes(10), "test");

        // when, then
        Assertions.assertThat(messageLog.canAddNewDetails()).isTrue();
    }

    @Test
    @DisplayName("취소된 로그는 플랫폼과 무관하게 상세 추가 불가")
    void canAddNewDetails_취소된로그_불가() {
        // given
        MessageLog messageLog = MessageLog.messageLog(
            channelOf(ChannelPlatform.KAKAO), null, LocalDateTime.now().plusDays(1), "test");
        messageLog.cancelMessageLog();

        // when, then
        Assertions.assertThat(messageLog.canAddNewDetails()).isFalse();
    }

    private Channel channelOf(ChannelPlatform platform) {
        Channel channel = Channel.createChannel("테스트채널", "테스트 설명");
        channel.updateChannelPlatform(platform);
        return channel;
    }

}
