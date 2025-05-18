package com.shrona.line_demo.line.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shrona.line_demo.line.application.sender.MessageSenderImpl;
import com.shrona.line_demo.line.domain.MessageLog;
import com.shrona.line_demo.line.infrastructure.sender.LineMessageSenderClient;
import com.shrona.line_demo.user.application.GroupService;
import com.shrona.line_demo.user.domain.Group;
import com.shrona.line_demo.user.domain.User;
import com.shrona.line_demo.user.domain.UserGroup;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientResponseException;

@ExtendWith(MockitoExtension.class)
public class LineMessageDeliveryTest {

    @Mock
    private MessageService messageService;

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
        Group mockGroup = mock(Group.class);
        UserGroup mockUserGroup = mock(UserGroup.class);
        User mockUser = mock(User.class);

        when(messageService.findReservedMessage())
            .thenReturn(List.of(mockMessageLog));
        when(mockMessageLog.getGroup()).thenReturn(mockGroup);
        when(mockGroup.getId()).thenReturn(1L);
        when(groupService.findGroupById(1L, true))
            .thenReturn(mockGroup);
        when(mockGroup.getUserGroupList()).thenReturn(List.of(mockUserGroup));
        when(mockUserGroup.getUser()).thenReturn(mockUser);
        when(mockUser.getLineId()).thenReturn("LINE_USER_001");

        // when
        messageSender.sendLineMessageByReservation();

        // then
        // 3. 검증: 메시지 전송 및 상태 변경 호출 확인
        verify(lineMessageSenderClient).SendMulticastMessage(any());
        verify(mockMessageLog).changeStatusAfterSend();

    }

    @Test
    void 예약_메시지_전송_실패_예외_처리_테스트() {
        // given
        MessageLog mockMessageLog = mock(MessageLog.class);
        Group mockGroup = mock(Group.class);

        when(messageService.findReservedMessage())
            .thenReturn(List.of(mockMessageLog));
        when(mockMessageLog.getGroup()).thenReturn(mockGroup);
        when(mockGroup.getId()).thenReturn(1L);
        when(groupService.findGroupById(1L, true))
            .thenReturn(mockGroup);
        doThrow(new RestClientResponseException("Error", 500, "Error", null, null, null))
            .when(lineMessageSenderClient).SendMulticastMessage(any());

        // when
        messageSender.sendLineMessageByReservation();

        // then
        verify(mockMessageLog, never()).changeStatusAfterSend();
    }

}
