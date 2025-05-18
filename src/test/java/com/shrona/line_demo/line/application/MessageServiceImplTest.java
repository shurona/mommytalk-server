package com.shrona.line_demo.line.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shrona.line_demo.line.domain.MessageLog;
import com.shrona.line_demo.line.domain.MessageType;
import com.shrona.line_demo.user.domain.Group;
import com.shrona.line_demo.user.infrastructure.GroupJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class MessageServiceImplTest {

    @Autowired
    private MessageServiceImpl messageService;
    @Autowired
    private GroupJpaRepository groupJpaRepository;

    @Test
    public void 메시지_저장_테스트() {
        // given
        MessageType mt = messageService.createMessageType("타이틀", "예시 포맷");

        LocalDateTime reserveTime = LocalDateTime.now();
        Long groupId = 1L;
        String content = "content";
        Group groupInfo = groupJpaRepository.save(Group.createGroup("name", "description"));

        // when
        List<MessageLog> logList = messageService
            .createMessage(
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
        MessageType mt = messageService.createMessageType("타이틀", "예시 포맷");

        LocalDateTime reserveTime = LocalDateTime.now();
        String content = "content";
        Group groupInfo = groupJpaRepository.save(Group.createGroup("name", "description"));

        // when
        for (int i = 0; i < 200; i++) {
            messageService
                .createMessage(mt.getId(), List.of(groupInfo.getId()), reserveTime.plusHours(i),
                    content);
        }

        // then
        Page<MessageLog> first = messageService.findMessageLogList(
            PageRequest.of(0, 100, Sort.by("reserveTime").descending()));
        assertThat(first.toList().size()).isEqualTo(100);

        Page<MessageLog> second = messageService.findMessageLogList(
            PageRequest.of(1, 100, Sort.by("reserveTime").descending()));
        assertThat(second.toList().size()).isEqualTo(100);

        Page<MessageLog> third = messageService.findMessageLogList(
            PageRequest.of(2, 100, Sort.by("reserveTime").descending()));
        assertThat(third.toList().size()).isEqualTo(0);

    }

    @Test
    public void 예약시간이된_메시지_호출_테스트() {
        // given
        MessageType mt = messageService.createMessageType("타이틀", "예시 포맷");

        LocalDateTime reserveTime = LocalDateTime.now();
        String content = "content";
        Group groupInfo = groupJpaRepository.save(Group.createGroup("name", "description"));

        // when
        for (int i = 0; i < 30; i++) {
            messageService
                .createMessage(mt.getId(), List.of(groupInfo.getId()), reserveTime.plusHours(3),
                    content);
        }
        for (int i = 0; i < 15; i++) {
            messageService
                .createMessage(mt.getId(), List.of(groupInfo.getId()), reserveTime.minusHours(3),
                    content);
        }

        // when
        List<MessageLog> messageLogs = messageService.findReservedMessage();

        // then
        assertThat(messageLogs.size()).isEqualTo(15);

    }
}