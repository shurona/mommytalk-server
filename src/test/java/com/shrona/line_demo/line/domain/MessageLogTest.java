package com.shrona.line_demo.line.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.shrona.line_demo.line.common.exception.LineException;
import java.time.LocalDateTime;
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
            null, null, null, reserveTime, "원본 메시지", null, "푸터 링크");
        String newContent = "새로운 메시지";

        // when
        messageLog.updateMessage(newContent);

        // then
        Assertions.assertThat(messageLog.getContent()).isEqualTo(newContent);
    }

    @Test
    @DisplayName("메시지 내용 업데이트 실패 - 예약 시간 5분 이내일 경우")
    void updateMessageFailWhenWithinFiveMinutesOfReservation() {
        // given
        LocalDateTime reserveTime = LocalDateTime.now().plusMinutes(4);
        MessageLog messageLog = MessageLog.messageLog(
            null, null, null, reserveTime, "원본 메시지", null, null);
        String newContent = "새로운 메시지";

        // when, then
        Assertions.assertThatThrownBy(() -> messageLog.updateMessage(newContent))
            .isInstanceOf(LineException.class);

        // 메시지 내용이 변경되지 않았는지 확인
        assertEquals("원본 메시지", messageLog.getContent());
    }

}