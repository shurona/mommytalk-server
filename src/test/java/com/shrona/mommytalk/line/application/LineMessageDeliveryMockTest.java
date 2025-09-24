package com.shrona.mommytalk.line.application;

import com.shrona.mommytalk.group.application.GroupService;
import com.shrona.mommytalk.line.application.sender.LineMessageSenderLegacyImpl;
import com.shrona.mommytalk.line.infrastructure.sender.LineMessageSenderClient;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageLogJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LineMessageDeliveryMockTest {

    @Mock
    private MessageLogJpaRepository messageRepository;

    @Mock
    private GroupService groupService;

    @Mock
    private LineMessageSenderClient lineMessageSenderClient;

    @InjectMocks
    private LineMessageSenderLegacyImpl messageSender; // 테스트 대상 클래스


    @Test
    public void 라인_메시지_발송_테스트() {
        //

    }

    @Test
    void 예약_메시지_전송_실패_예외_처리_테스트() {
        //
    }

}
