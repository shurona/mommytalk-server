package com.shrona.mommytalk.message.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.channel.domain.ChannelPlatform;
import com.shrona.mommytalk.entitlement.domain.Entitlement;
import com.shrona.mommytalk.entitlement.domain.EntitlementType;
import com.shrona.mommytalk.entitlement.infrastructure.jpa.EntitlementJpaRepository;
import com.shrona.mommytalk.group.domain.Group;
import com.shrona.mommytalk.group.domain.GroupType;
import com.shrona.mommytalk.group.domain.UserGroup;
import com.shrona.mommytalk.group.infrastructure.repository.jpa.GroupJpaRepository;
import com.shrona.mommytalk.line.infrastructure.repository.jpa.ChannelJpaRepository;
import com.shrona.mommytalk.message.domain.MessageContent;
import com.shrona.mommytalk.message.domain.MessageLog;
import com.shrona.mommytalk.message.domain.MessageLogDetail;
import com.shrona.mommytalk.message.domain.MessageType;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageContentJpaRepository;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageLogDetailJpaRepository;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageLogJpaRepository;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageTypeJpaRepository;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import com.shrona.mommytalk.user.infrastructure.repository.jpa.UserJpaRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class MessageLogDetailServiceImplTest {

    @Autowired
    private MessageLogDetailServiceImpl messageLogDetailService;


    @Autowired
    private MessageLogJpaRepository messageLogJpaRepository;

    @Autowired
    private MessageLogDetailJpaRepository messageLogDetailJpaRepository;

    @Autowired
    private ChannelJpaRepository channelJpaRepository;

    @Autowired
    private MessageTypeJpaRepository messageTypeJpaRepository;

    @Autowired
    private MessageContentJpaRepository messageContentJpaRepository;

    @Autowired
    private GroupJpaRepository groupJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private EntitlementJpaRepository entitlementJpaRepository;

    @Autowired
    private EntityManager entityManager;

    private Channel channel;
    private MessageType messageType;
    private MessageContent messageContent;
    private Group entitlementGroup;
    private Group includeGroup;
    private Group exceptGroup;
    private User user1;  // entitlementGroup
    private User user2;  // includeGroup
    private User user3;  // exceptGroup
    private User user4;  // 신규 유저 (entitlementGroup)

    @BeforeEach
    void setUp() {
        // 1. Channel 생성
        channel = channelJpaRepository.save(
            Channel.createChannel("테스트채널", "테스트 설명")
        );
        channel.updateChannelPlatform(ChannelPlatform.KAKAO);

        // 2. Entitlement 생성
        Entitlement entitlement = entitlementJpaRepository.save(
            Entitlement.createEntitlement("MOMMYTALK", EntitlementType.MOMMYTALK)
        );

        // 3. MessageType 생성
        messageType = messageTypeJpaRepository.save(
            MessageType.of(
                "테스트 주제",
                "테스트 컨텍스트",
                LocalDate.now().plusDays(1),
                channel
            )
        );

        // 4. MessageContent 생성
        messageContent = messageContentJpaRepository.save(
            MessageContent.of(messageType, "테스트 메시지", 2, 2)
        );

        // 5. Group 생성
        entitlementGroup = groupJpaRepository.save(
            Group.createEntitlementGroup(
                channel,
                entitlement,
                GroupType.AUTO_ACTIVE,
                "상품그룹",
                "상품 설명"
            )
        );

        includeGroup = groupJpaRepository.save(
            Group.createGroup(channel, "포함그룹", "포함 설명")
        );

        exceptGroup = groupJpaRepository.save(
            Group.createGroup(channel, "제외그룹", "제외 설명")
        );

        // 6. User 생성 및 그룹 추가
        // user1: entitlementGroup 소속
        user1 = userJpaRepository.save(
            User.createUser(new PhoneNumber("01012345678"))
        );
        user1.updateUserFromRequest(null, 2, 2);

        UserGroup userGroup1 = UserGroup.createUserGroup(user1, entitlementGroup);
        entitlementGroup.addUserToGroup(List.of(userGroup1));

        // user2: includeGroup 소속
        user2 = userJpaRepository.save(
            User.createUser(new PhoneNumber("01087654321"))
        );
        user2.updateUserFromRequest(null, 2, 2);

        UserGroup userGroup2 = UserGroup.createUserGroup(user2, includeGroup);
        includeGroup.addUserToGroup(List.of(userGroup2));

        // user3: exceptGroup 소속
        user3 = userJpaRepository.save(
            User.createUser(new PhoneNumber("01011112222"))
        );
        user3.updateUserFromRequest(null, 2, 2);

        UserGroup userGroup3 = UserGroup.createUserGroup(user3, exceptGroup);
        exceptGroup.addUserToGroup(List.of(userGroup3));

        // user4: entitlementGroup 소속 (신규 유저)
        user4 = userJpaRepository.save(
            User.createUser(new PhoneNumber("01099998888"))
        );
        user4.updateUserFromRequest(null, 2, 2);

        UserGroup userGroup4 = UserGroup.createUserGroup(user4, entitlementGroup);
        entitlementGroup.addUserToGroup(List.of(userGroup4));

        entityManager.flush();
        entityManager.clear();

    }

    @Test
    @DisplayName("상품그룹_포함그룹_제외그룹_유저조회_검증_테스트")
    void 상품그룹_포함그룹_제외그룹_유저조회_검증_테스트() {

        messageType = messageTypeJpaRepository.findById(messageType.getId()).orElseThrow();

        // given
        // MessageLog 생성 (user1만 포함, user4는 누락)
        MessageLog messageLog = MessageLog.messageLog(
            channel,
            messageType,
            LocalDateTime.now().plusDays(1),
            "test"
        );

        // 그룹 정보 설정
        messageLog.updateGroupInfo(
            entitlementGroup,
            List.of(includeGroup.getId()),  // 포함 그룹
            List.of(exceptGroup.getId())    // 제외 그룹
        );

        // user1만 MessageLogDetail 생성 (user4는 누락)
        messageLog.addMessageLogDetailInfo(
            MessageLogDetail.createLogDetail(messageLog, user1, messageContent)
        );

        MessageLog savedMessageLog = messageLogJpaRepository.save(messageLog);

        // when: 누락 유저 추가 실행
        int addedCount = messageLogDetailService.addMissingDetailsBeforeSend(
            savedMessageLog.getId()
        );

        // then
        assertThat(addedCount).isEqualTo(2);  // user2(includeGroup), user4(신규)

        MessageLog reloadedMessageLog = messageLogJpaRepository.findById(savedMessageLog.getId())
            .orElseThrow();

        List<MessageLogDetail> details = reloadedMessageLog.getMessageLogDetailList();

        assertThat(details).hasSize(3);  // user1(기존) + user2 + user4

        // 유저 검증
        List<Long> userIds = details.stream()
            .map(d -> d.getUser().getId())
            .toList();

        assertThat(userIds).containsExactlyInAnyOrder(
            user1.getId(),  // 기존
            user2.getId(),  // includeGroup 신규
            user4.getId()   // entitlementGroup 신규
        );

        // user3(exceptGroup)는 제외됨
        assertThat(userIds).doesNotContain(user3.getId());
    }

    @Test
    @DisplayName("누락 유저가 없으면 0 반환")
    void 누락유저없음_0반환() {

        messageType = messageTypeJpaRepository.findById(messageType.getId()).orElseThrow();

        // given
        MessageLog messageLog = MessageLog.messageLog(
            channel,
            messageType,
            LocalDateTime.now().plusDays(1),
            "test"
        );

        messageLog.updateGroupInfo(
            entitlementGroup,
            List.of(includeGroup.getId()),
            List.of(exceptGroup.getId())
        );

        // 모든 유저 추가
        messageLog.addMessageLogDetailInfo(
            MessageLogDetail.createLogDetail(messageLog, user1, messageContent)
        );
        messageLog.addMessageLogDetailInfo(
            MessageLogDetail.createLogDetail(messageLog, user2, messageContent)
        );
        messageLog.addMessageLogDetailInfo(
            MessageLogDetail.createLogDetail(messageLog, user4, messageContent)
        );

        MessageLog savedMessageLog = messageLogJpaRepository.save(messageLog);

        // when
        int addedCount = messageLogDetailService.addMissingDetailsBeforeSend(
            savedMessageLog.getId()
        );

        // then
        assertThat(addedCount).isZero();
    }

}