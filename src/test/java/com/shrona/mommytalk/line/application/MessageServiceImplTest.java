package com.shrona.mommytalk.line.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.entitlement.domain.Entitlement;
import com.shrona.mommytalk.entitlement.domain.EntitlementType;
import com.shrona.mommytalk.entitlement.infrastructure.jpa.EntitlementJpaRepository;
import com.shrona.mommytalk.group.domain.Group;
import com.shrona.mommytalk.group.domain.UserGroup;
import com.shrona.mommytalk.group.infrastructure.repository.jpa.GroupJpaRepository;
import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.line.infrastructure.repository.jpa.ChannelJpaRepository;
import com.shrona.mommytalk.line.infrastructure.repository.jpa.LineUserJpaRepository;
import com.shrona.mommytalk.message.application.MessageServiceImpl;
import com.shrona.mommytalk.message.application.MessageTypeServiceImpl;
import com.shrona.mommytalk.message.common.utils.MessageUtils;
import com.shrona.mommytalk.message.domain.MessageContent;
import com.shrona.mommytalk.message.domain.MessageLog;
import com.shrona.mommytalk.message.domain.MessageType;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageContentJpaRepository;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import com.shrona.mommytalk.user.infrastructure.repository.jpa.UserJpaRepository;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class MessageServiceImplTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntitlementJpaRepository entitlementJpaRepository;
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
    private MessageContentJpaRepository messageContentJpaRepository;
    @MockitoBean
    private MessageUtils messageUtils;

    private Channel channel;
    private Channel channel2;
    private Group groupInfo;
    private MessageType mt;
    private Entitlement entitlement;
    private LocalDate currentDate;

    @BeforeEach
    public void beforeEach() {
        // 테스트마다 현재 날짜를 새로 설정
        currentDate = LocalDate.now();

        entitlement = entitlementJpaRepository.save(
            Entitlement.createEntitlement("마미톡", EntitlementType.MOMMYTALK));

        channel = channelRepository.save(Channel.createChannel("이름", "설명"));
        channel2 = channelRepository.save(Channel.createChannel("이름2", "설명"));
        mt = messageTypeService.createMessageType("타이틀", "예시 포맷", currentDate, channel);
        // 전날 날짜로도 MessageType 생성 (reserveTime.minusHours(3) 대응)
        messageTypeService.createMessageType("전날 타이틀", "예시 포맷", currentDate.minusDays(1), channel);

        MessageType mt2 = messageTypeService.createMessageType("두번째", "예시", currentDate, channel2);
        // channel2 전날 날짜용 MessageType
        messageTypeService.createMessageType("두번째 전날", "예시", currentDate.minusDays(1), channel2);

        Group beforeSave = Group.createGroup(channel, "name", "description");
        beforeSave.updateGroupEntitlement(entitlement);
        groupInfo = groupJpaRepository.save(beforeSave);
        messageContentJpaRepository.save(
            MessageContent.of(mt, "컨텐츠", 2, 2));

        entityManager.flush();
        entityManager.clear();

    }

    @Test
    public void 날짜로_메시지_조회() {

        MessageType messageTypeByDate = messageTypeService.findMessageTypeByDate(
            currentDate, channel);

        Channel channel3 = channelRepository.save(Channel.createChannel("이름2", "설명"));

        assertThat(messageTypeByDate.getId()).isEqualTo(mt.getId());
        MessageType messageTypeByDate1 = messageTypeService.findMessageTypeByDate(currentDate,
            channel3);

        assertThat(messageTypeByDate1).isNull();
    }

    @Test
    public void 메시지_저장_테스트() {
        // given
        // message 전달은 mocking
        doNothing().when(messageUtils).registerTaskSchedule(anyList(), any(LocalDateTime.class));

        LocalDateTime reserveTime = LocalDateTime.now();
        String content = "content";

        List<User> users = saveUserAndGetUsers("1234", 1, 0);
        groupInfo.addUserToGroup(
            users.stream().map(
                u -> UserGroup.createUserGroup(u, groupInfo)
            ).toList()
        );
        groupJpaRepository.save(groupInfo);

        // when
        List<MessageLog> logList = messageService
            .createMessageSelectGroup(channel,
                groupInfo.getId(), List.of(2L), new ArrayList<>(),
                reserveTime.plusHours(5), content);
        MessageLog afterSaveLog = messageService.findByMessageId(logList.getFirst().getId());

        // then
        assertThat(logList.size()).isEqualTo(1);
        assertThat(afterSaveLog.getGroupInfo()).isEqualTo(content);
        assertThat(afterSaveLog.getReserveTime()).isEqualTo(reserveTime.plusHours(5));
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
        includeGroupInfo.updateGroupEntitlement(entitlement);

        exceptGroupInfo.addUserToGroup(
            userList.subList(40, 80).stream().map(
                u -> UserGroup.createUserGroup(u, exceptGroupInfo)
            ).toList()
        );

        groupJpaRepository.saveAll(
            List.of(groupInfo, includeGroupInfo, exceptGroupInfo, exceptSecondGroupInfo));

        LocalDateTime reserveTime = LocalDateTime.now();
        String content = "content";

        // when
        List<MessageLog> messageLogListOne = messageService
            .createMessageSelectGroup(channel, groupInfo.getId(),
                List.of(includeGroupInfo.getId()),
                List.of(exceptGroupInfo.getId()),
                reserveTime.plusHours(1), content);

        List<MessageLog> messageLogListTwo = messageService
            .createMessageSelectGroup(channel, includeGroupInfo.getId(),
                List.of(includeGroupInfo.getId()),
                List.of(exceptGroupInfo.getId()),
                reserveTime.plusHours(1), content);

        // then
        MessageLog first = messageService.findByMessageId(messageLogListOne.getFirst().getId());
        MessageLog last = messageService.findByMessageId(messageLogListTwo.getFirst().getId());
        assertThat(first.getMessageLogDetailList().size()).isEqualTo(160);
        assertThat(last.getMessageLogDetailList().size()).isEqualTo(40);
    }

    @Test
    public void 예약시간이된_메시지_호출_테스트() {
        // given
        // message 전달은 mocking
        doNothing().when(messageUtils).registerTaskSchedule(anyList(), any(LocalDateTime.class));

        LocalDateTime reserveTime = currentDate.atStartOfDay();
        String content = "content";

        List<User> userList = saveUserAndGetUsers("1234", 1, 1);
        groupInfo.addUserToGroup(
            userList.stream().map(
                u -> UserGroup.createUserGroup(u, groupInfo)
            ).toList()
        );
        groupJpaRepository.save(groupInfo);

        // when
        // 이후 시간으로 추가
        for (int i = 0; i < 30; i++) {
            messageService
                .createMessageSelectGroup(channel, groupInfo.getId(),
                    List.of(groupInfo.getId()), new ArrayList<>(),
                    reserveTime.plusHours(3), content);
        }
        // 이전 시간으로 추가(reserveList로 조회될 크기)
        for (int i = 0; i < 15; i++) {
            messageService
                .createMessageSelectGroup(channel, groupInfo.getId(),
                    List.of(groupInfo.getId()), new ArrayList<>(),
                    reserveTime.minusHours(3), content);
        }
        // 다른 채널에 추가
        for (int i = 0; i < 2; i++) {
            messageService
                .createMessageSelectGroup(channel2, groupInfo.getId(),
                    List.of(groupInfo.getId()), new ArrayList<>(),
                    reserveTime.minusHours(3), content);
        }

        // when
        Page<MessageLog> allMessage = messageService.findMessageLogList(
            channel, PageRequest.of(0, 100));

        // then
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
            .createMessageSelectGroup(channel, groupInfo.getId(),
                List.of(groupInfo.getId()), new ArrayList<>(),
                reserveTime.plusHours(3), content);

        // when
        MessageLog messageLog = messageService.findByMessageId(messageLogList.getFirst().getId());
        Map<Long, Integer> lineIdCountByLog = messageService.findLineIdCountByLog(
            List.of(messageLogList.getFirst().getId()));

        // then
        assertThat(messageLog.getMessageLogDetailList().size()).isEqualTo(3);
        assertThat(lineIdCountByLog.get(messageLog.getId())).isEqualTo(3);
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