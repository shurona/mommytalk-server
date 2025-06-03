package com.shrona.line_demo.line.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;

import com.shrona.line_demo.line.application.utils.MessageUtils;
import com.shrona.line_demo.line.domain.Channel;
import com.shrona.line_demo.line.domain.LineUser;
import com.shrona.line_demo.line.domain.MessageLog;
import com.shrona.line_demo.line.domain.MessageType;
import com.shrona.line_demo.line.infrastructure.ChannelJpaRepository;
import com.shrona.line_demo.line.infrastructure.LineUserJpaRepository;
import com.shrona.line_demo.user.domain.Group;
import com.shrona.line_demo.user.domain.User;
import com.shrona.line_demo.user.domain.UserGroup;
import com.shrona.line_demo.user.domain.vo.PhoneNumber;
import com.shrona.line_demo.user.infrastructure.GroupJpaRepository;
import com.shrona.line_demo.user.infrastructure.UserJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    private UserJpaRepository userJpaRepository;
    @Autowired
    private LineUserJpaRepository lineUserJpaRepository;
    @Autowired
    private ChannelJpaRepository channelRepository;
    @MockitoBean
    private MessageUtils messageUtils;

    private Channel channel;
    private Channel channel2;

    private Group groupInfo;
    private MessageType mt;

    @BeforeEach
    public void beforeEach() {
        channel = channelRepository.save(Channel.createChannel("이름", "설명"));
        channel2 = channelRepository.save(Channel.createChannel("이름2", "설명"));
        mt = messageService.createMessageType("타이틀", "예시 포맷");

        Group beforeSave = Group.createGroup(channel, "name", "description");
        groupInfo = groupJpaRepository.save(beforeSave);


    }

    @Test
    public void 메시지_저장_테스트() {
        // given
        // message 전달은 mocking
        doNothing().when(messageUtils).registerTaskSchedule(anyList(), any(LocalDateTime.class));

        LocalDateTime reserveTime = LocalDateTime.now();
        String content = "content";

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

        LocalDateTime reserveTime = LocalDateTime.now();
        String content = "content";

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

        LocalDateTime reserveTime = LocalDateTime.now();
        String content = "content";

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

    @DisplayName("로그에 속한 라인아이디조회")
    @Test
    public void 로그에속한_라인아이디조회() {
        // given
        // message 전달은 mocking
        doNothing().when(messageUtils).registerTaskSchedule(anyList(), any(LocalDateTime.class));

        LocalDateTime reserveTime = LocalDateTime.now();
        String content = "content";
        List<User> userList = saveUserAndGetUsers();

        groupInfo.addUserToGroup(
            userList.stream().map(
                u -> UserGroup.createUserGroup(u, groupInfo)
            ).toList()
        );
        groupJpaRepository.save(groupInfo);

        List<MessageLog> messageLogList = messageService
            .createMessageSelectGroup(channel, mt.getId(), List.of(groupInfo.getId()),
                reserveTime.plusHours(3),
                content);

        // when
        MessageLog messageLog = messageService.findByMessageId(messageLogList.getFirst().getId());
        Map<Long, Integer> lineIdCountByLog = messageService.findLineIdCountByLog(
            List.of(messageLogList.getFirst().getId()));

        // then
        assertThat(messageLog.getMessageLogLineInfoList().size()).isEqualTo(2);
        assertThat(lineIdCountByLog.get(messageLog.getId())).isEqualTo(2);
    }

    private List<User> saveUserAndGetUsers() {
        List<LineUser> lineUsers = lineUserJpaRepository.saveAll(
            List.of(LineUser.createLineUser("line1"), LineUser.createLineUser("line2")));
        return userJpaRepository.saveAll(
            List.of(
                User.createUserWithLine(PhoneNumber.changeWithoutError("010-1234-1235"),
                    lineUsers.get(1)),
                User.createUserWithLine(PhoneNumber.changeWithoutError("010-1234-1234"),
                    lineUsers.get(0)),
                User.createUser(PhoneNumber.changeWithoutError("010-1234-1236"))

            )
        );

    }
}