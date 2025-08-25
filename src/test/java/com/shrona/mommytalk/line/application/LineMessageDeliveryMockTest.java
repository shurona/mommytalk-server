package com.shrona.mommytalk.line.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shrona.mommytalk.line.application.sender.MessageSenderImpl;
import com.shrona.mommytalk.line.domain.Channel;
import com.shrona.mommytalk.line.domain.MessageLog;
import com.shrona.mommytalk.line.domain.MessageLogLineInfo;
import com.shrona.mommytalk.line.infrastructure.MessageLogJpaRepository;
import com.shrona.mommytalk.line.infrastructure.sender.LineMessageSenderClient;
import com.shrona.mommytalk.user.application.GroupService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientResponseException;

@ExtendWith(MockitoExtension.class)
public class LineMessageDeliveryMockTest {

    @Mock
    private MessageLogJpaRepository messageRepository;

    @Mock
    private GroupService groupService;

    @Mock
    private LineMessageSenderClient lineMessageSenderClient;

    @InjectMocks
    private MessageSenderImpl messageSender; // 테스트 대상 클래스


    @Test
    public void 라인_메시지_발송_테스트() {

        // given
        MessageLog mockMessageLog = mock(MessageLog.class);
        MessageLogLineInfo mockML = mock(MessageLogLineInfo.class);
        Channel mockChannel = mock(Channel.class);

        when(messageRepository.findAllByReservedMessageBeforeNow(
            any(LocalDateTime.class)))
            .thenReturn(List.of(mockMessageLog));
        when(mockMessageLog.getMessageLogLineInfoList()).thenReturn(List.of(mockML));
        when(mockMessageLog.getChannel()).thenReturn(mockChannel);

        when(mockML.getLineId()).thenReturn("LINE_USER_001");

        when(mockChannel.getAccessToken()).thenReturn("asdfasdfasdfasdf");

        // when
        messageSender.sendLineMessageByReservation();

        // then
        // 3. 검증: 메시지 전송 및 상태 변경 호출 확인
        verify(lineMessageSenderClient).SendMulticastMessage(anyString(), any());
        verify(mockMessageLog).changeStatusAfterSend();

    }

    @Test
    void 예약_메시지_전송_실패_예외_처리_테스트() {
        // given
        MessageLog mockMessageLog = mock(MessageLog.class);
        MessageLogLineInfo mockML = mock(MessageLogLineInfo.class);
        Channel mockChannel = mock(Channel.class);

        when(messageRepository.findAllByReservedMessageBeforeNow(
            any(LocalDateTime.class)))
            .thenReturn(List.of(mockMessageLog));
        when(mockMessageLog.getMessageLogLineInfoList()).thenReturn(List.of(mockML));
        when(mockMessageLog.getChannel()).thenReturn(mockChannel);

        when(mockML.getLineId()).thenReturn("LINE_USER_001");

        when(mockChannel.getAccessToken()).thenReturn("asdfasdfasdfasdf");
        doThrow(new RestClientResponseException("Error", 500, "Error", null, null, null))
            .when(lineMessageSenderClient).SendMulticastMessage(anyString(), any());

        // when
        messageSender.sendLineMessageByReservation();

        // then
        verify(mockMessageLog, never()).changeStatusAfterSend();
    }

}
