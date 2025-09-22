package com.shrona.mommytalk.line.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.group.domain.Group;
import com.shrona.mommytalk.group.domain.UserGroup;
import com.shrona.mommytalk.group.infrastructure.repository.GroupJpaRepository;
import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.line.infrastructure.ChannelJpaRepository;
import com.shrona.mommytalk.line.infrastructure.LineUserJpaRepository;
import com.shrona.mommytalk.message.application.MessageServiceImpl;
import com.shrona.mommytalk.message.application.MessageTypeServiceImpl;
import com.shrona.mommytalk.message.common.utils.MessageUtils;
import com.shrona.mommytalk.message.domain.MessageLog;
import com.shrona.mommytalk.message.domain.MessageType;
import com.shrona.mommytalk.message.domain.ScheduledMessageText;
import com.shrona.mommytalk.message.infrastructure.repository.ScheduledMessageTextJpaRepository;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import com.shrona.mommytalk.user.infrastructure.UserJpaRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
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
    private EntityManager entityManager;

    @Autowired
    private MessageServiceImpl messageService;
    @Autowired
    private MessageTypeServiceImpl messageTypeService;
    @Autowired
    private GroupJpaRepository groupJpaRepository;
    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private LineUserJpaRepository lineUserJpaRepository;
    @Autowired
    private ChannelJpaRepository channelRepository;
    @Autowired
    private ScheduledMessageTextJpaRepository scheduledMessageTextJpaRepository;
    @MockitoBean
    private MessageUtils messageUtils;

    private Channel channel;
    private Channel channel2;
    private Group groupInfo;
    private MessageType mt;
    private LocalDate currentDate = LocalDate.now();

    @BeforeEach
    public void beforeEach() {
        channel = channelRepository.save(Channel.createChannel("이름", "설명"));
        channel2 = channelRepository.save(Channel.createChannel("이름2", "설명"));
        mt = messageTypeService.createMessageType("타이틀", "예시 포맷", currentDate);

        Group beforeSave = Group.createGroup(channel, "name", "description");
        groupInfo = groupJpaRepository.save(beforeSave);
        scheduledMessageTextJpaRepository.save(
            ScheduledMessageText.of(mt, "컨텐츠", 2, 2));

        entityManager.flush();
        entityManager.clear();

    }

    @Test
    public void 날짜로_메시지_조회() {

        MessageType messageTypeByDate = messageTypeService.findMessageTypeByDate(currentDate);

        assertThat(messageTypeByDate.getId()).isEqualTo(mt.getId());
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
            .createMessageSelectGroup(channel, mt.getId(),
                List.of(groupInfo.getId(), 2L), new ArrayList<>(),
                reserveTime.plusHours(5), content);
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
                .createMessageSelectGroup(channel, mt.getId(),
                    List.of(groupInfo.getId()), new ArrayList<>(),
                    reserveTime, content);
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
    public void 메시지_제외_그룹_확인_조회_테스트() {
        // given
        // message 전달은 mocking
        doNothing().when(messageUtils).registerTaskSchedule(anyList(), any(LocalDateTime.class));

        Group includeGroupInfo = groupJpaRepository.save(
            Group.createGroup(channel, "include", "description")
        );

        Group exceptGroupInfo = groupJpaRepository.save(
            Group.createGroup(channel, "except", "description")
        );

        Group exceptSecondGroupInfo = groupJpaRepository.save(
            Group.createGroup(channel, "except-2", "description")
        );

        List<User> userList = saveUserAndGetUsers("1234", 100, 100);
        groupInfo.addUserToGroup(
            userList.stream().map(
                u -> UserGroup.createUserGroup(u, groupInfo)
            ).toList()
        );

        includeGroupInfo.addUserToGroup(
            userList.subList(0, 50).stream().map(
                u -> UserGroup.createUserGroup(u, includeGroupInfo)
            ).toList()
        );

        exceptGroupInfo.addUserToGroup(
            userList.subList(40, 80).stream().map(
                u -> UserGroup.createUserGroup(u, exceptGroupInfo)
            ).toList()
        );

//        exceptSecondGroupInfo.addUserToGroup(
//            userList.subList(20, 88).stream().map(
//                u -> UserGroup.createUserGroup(u, exceptGroupInfo)
//            ).toList()
//        );

        groupJpaRepository.saveAll(
            List.of(groupInfo, includeGroupInfo, exceptGroupInfo, exceptSecondGroupInfo));

        LocalDateTime reserveTime = LocalDateTime.now();
        String content = "content";

        // when
        List<MessageLog> messageLogList = messageService
            .createMessageSelectGroup(channel, mt.getId(),
                List.of(groupInfo.getId(), includeGroupInfo.getId()),
                List.of(exceptGroupInfo.getId()),
                reserveTime.plusHours(1), content);

        // then
        MessageLog first = messageService.findByMessageId(messageLogList.getFirst().getId());
        MessageLog last = messageService.findByMessageId(messageLogList.getLast().getId());
        assertThat(first.getMessageLogDetailInfoList().size()).isEqualTo(160);
        assertThat(last.getMessageLogDetailInfoList().size()).isEqualTo(40);
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
                .createMessageSelectGroup(channel, mt.getId(),
                    List.of(groupInfo.getId()), new ArrayList<>(),
                    reserveTime.plusHours(3), content);
        }
        // 이전 시간으로 추가(reserveList로 조회될 크기)
        for (int i = 0; i < 15; i++) {
            messageService
                .createMessageSelectGroup(channel, mt.getId(),
                    List.of(groupInfo.getId()), new ArrayList<>(),
                    reserveTime.minusHours(3), content);
        }
        // 다른 채널에 추가
        for (int i = 0; i < 2; i++) {
            messageService
                .createMessageSelectGroup(channel2, mt.getId(),
                    List.of(groupInfo.getId()), new ArrayList<>(),
                    reserveTime.minusHours(3), content);
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
        List<User> userList = saveUserAndGetUsers("1234", 2, 1);

        groupInfo.addUserToGroup(
            userList.stream().map(
                u -> UserGroup.createUserGroup(u, groupInfo)
            ).toList()
        );
        groupJpaRepository.save(groupInfo);

        List<MessageLog> messageLogList = messageService
            .createMessageSelectGroup(channel, mt.getId(),
                List.of(groupInfo.getId()), new ArrayList<>(),
                reserveTime.plusHours(3), content);

        // when
        MessageLog messageLog = messageService.findByMessageId(messageLogList.getFirst().getId());
        Map<Long, Integer> lineIdCountByLog = messageService.findLineIdCountByLog(
            List.of(messageLogList.getFirst().getId()));

        // then
        assertThat(messageLog.getMessageLogDetailInfoList().size()).isEqualTo(2);
        assertThat(lineIdCountByLog.get(messageLog.getId())).isEqualTo(2);
    }

    /**
     * 유저와 라인 유저들을 만들고 생성된 유저 목록을 반환해준다.
     */
    private List<User> saveUserAndGetUsers(String middle, int lintCt, int userCt) {

        // 라인 유저 저장
        List<LineUser> lineForSaveList = new ArrayList<>();
        for (int i = 0; i < lintCt; i++) {
            lineForSaveList.add(
                LineUser.createLineUser("line" + i)
            );
        }
        List<LineUser> lineUsers = lineUserJpaRepository.saveAll(lineForSaveList);

        List<User> userForSaveList = new ArrayList<>();
        for (int i = 0; i < userCt; i++) {
            String phoneN = "010-" + middle + "-" + String.valueOf(1000 + i);
            User tpUser = User.createUser(PhoneNumber.changeWithoutError(phoneN));
            tpUser.updateUserFromRequest("미나", 2, 2);
            userForSaveList.add(tpUser);
        }
        List<User> userList = userJpaRepository.saveAll(userForSaveList);

        List<User> userWithLineForSaveList = new ArrayList<>();
        for (int i = 0; i < lintCt; i++) {
            String phoneN = "010-" + middle + "-" + String.valueOf(2000 + i);
            User tpUser = User.createUserWithLine(PhoneNumber.changeWithoutError(phoneN),
                lineUsers.get(i));
            tpUser.updateUserFromRequest("미나", 2, 2);
            userWithLineForSaveList.add(tpUser);
        }
        List<User> userListWithLine = userJpaRepository.saveAll(userWithLineForSaveList);

        return Stream.concat(userListWithLine.stream(), userList.stream()).toList();
    }
}