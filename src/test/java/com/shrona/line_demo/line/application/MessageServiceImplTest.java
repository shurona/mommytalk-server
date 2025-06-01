package com.shrona.line_demo.line.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;

import com.shrona.line_demo.line.application.utils.MessageUtils;
import com.shrona.line_demo.line.domain.Channel;
import com.shrona.line_demo.line.domain.MessageLog;
import com.shrona.line_demo.line.domain.MessageType;
import com.shrona.line_demo.line.infrastructure.ChannelJpaRepository;
import com.shrona.line_demo.user.domain.Group;
import com.shrona.line_demo.user.infrastructure.GroupJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class MessageServiceImplTest {


    @Autowired
    private MessageServiceImpl messageService;
    @Autowired
    private GroupJpaRepository groupJpaRepository;
    @Autowired
    private ChannelJpaRepository channelRepository;
    @MockitoBean
    private MessageUtils messageUtils;

    private Channel channel;
    private Channel channel2;

    @BeforeEach
    public void beforeEach() {
        channel = channelRepository.save(Channel.createChannel("이름", "설명"));
        channel2 = channelRepository.save(Channel.createChannel("이름2", "설명"));
    }

    @Test
    public void 메시지_저장_테스트() {
        // given
        // message 전달은 mocking
        doNothing().when(messageUtils).registerTaskSchedule(anyList(), any(LocalDateTime.class));

        MessageType mt = messageService.createMessageType("타이틀", "예시 포맷");

        LocalDateTime reserveTime = LocalDateTime.now();
        Long groupId = 1L;
        String content = "content";
        Group groupInfo = groupJpaRepository.save(
            Group.createGroup(channel, "name", "description"));

        // when
        List<MessageLog> logList = messageService
            .createMessageSelectGroup(channel,
                mt.getId(), List.of(groupInfo.getId(), 2L), reserveTime.plusHours(5), content);
        MessageLog afterSaveLog = messageService.findByMessageId(logList.getFirst().getId());

        // then
        assertThat(logList.size()).isEqualTo(1);
        assertThat(afterSaveLog.getContent()).isEqualTo(content);
        assertThat(afterSaveLog.getReserveTime()).isEqualTo(reserveTime.plusHours(5));
    }

    @Test
    public void 메시지_목록_조회_테스트() {
        // given
        // message 전달은 mocking
        doNothing().when(messageUtils).registerTaskSchedule(anyList(), any(LocalDateTime.class));

        MessageType mt = messageService.createMessageType("타이틀", "예시 포맷");
        LocalDateTime reserveTime = LocalDateTime.now();
        String content = "content";
        Group groupInfo = groupJpaRepository.save(
            Group.createGroup(channel, "name", "description"));

        // when
        for (int i = 0; i < 200; i++) {
            messageService
                .createMessageSelectGroup(channel, mt.getId(), List.of(groupInfo.getId()),
                    reserveTime.plusHours(i),
                    content);
        }

        // then
        Page<MessageLog> first = messageService.findMessageLogList(channel,
            PageRequest.of(0, 100, Sort.by("reserveTime").descending()));
        assertThat(first.toList().size()).isEqualTo(100);

        Page<MessageLog> second = messageService.findMessageLogList(channel,
            PageRequest.of(1, 100, Sort.by("reserveTime").descending()));
        assertThat(second.toList().size()).isEqualTo(100);

        Page<MessageLog> third = messageService.findMessageLogList(channel,
            PageRequest.of(2, 100, Sort.by("reserveTime").descending()));
        assertThat(third.toList().size()).isEqualTo(0);

    }

    @Test
    public void 예약시간이된_메시지_호출_테스트() {
        // given
        // message 전달은 mocking
        doNothing().when(messageUtils).registerTaskSchedule(anyList(), any(LocalDateTime.class));

        MessageType mt = messageService.createMessageType("타이틀", "예시 포맷");

        LocalDateTime reserveTime = LocalDateTime.now();
        String content = "content";
        Group groupInfo = groupJpaRepository.save(
            Group.createGroup(channel, "name", "description"));

        // when
        // 이후 시간으로 추가
        for (int i = 0; i < 30; i++) {
            messageService
                .createMessageSelectGroup(channel, mt.getId(), List.of(groupInfo.getId()),
                    reserveTime.plusHours(3),
                    content);
        }
        // 이전 시간으로 추가(reserveList로 조회될 크기)
        for (int i = 0; i < 15; i++) {
            messageService
                .createMessageSelectGroup(channel, mt.getId(), List.of(groupInfo.getId()),
                    reserveTime.minusHours(3),
                    content);
        }
        // 다른 채널에 추가
        for (int i = 0; i < 2; i++) {
            messageService
                .createMessageSelectGroup(channel2, mt.getId(), List.of(groupInfo.getId()),
                    reserveTime.minusHours(3),
                    content);
        }

        // when
        List<MessageLog> messageLogs = messageService.findReservedMessage(channel);
        Page<MessageLog> allMessage = messageService.findMessageLogList(
            channel, PageRequest.of(0, 100));

        // then
        assertThat(messageLogs.size()).isEqualTo(15);
        assertThat(allMessage.toList().size()).isEqualTo(45);

    }
}