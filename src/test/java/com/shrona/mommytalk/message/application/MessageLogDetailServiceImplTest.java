package com.shrona.mommytalk.message.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.channel.domain.ChannelPlatform;
import com.shrona.mommytalk.common.utils.DateTimeUtils;
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
import com.shrona.mommytalk.message.domain.type.ReservationStatus;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageContentJpaRepository;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageLogDetailJpaRepository;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageLogJpaRepository;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageTypeJpaRepository;
import com.shrona.mommytalk.message.infrastructure.repository.query.MessageLogDetailQueryRepository;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import com.shrona.mommytalk.user.infrastructure.repository.jpa.UserJpaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * addMissingDetailsBeforeSend는 REQUIRES_NEW로 동작하므로 테스트 트랜잭션 안의 미커밋 데이터를 볼 수 없다.
 * 그래서 픽스처는 TransactionTemplate으로 커밋하고, 매 테스트 후 테이블을 비운다.
 */
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
    private MessageLogDetailQueryRepository messageLogDetailQueryRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private TransactionTemplate transactionTemplate;

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
        transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.executeWithoutResult(status -> {
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
        });
    }

    @AfterEach
    void tearDown() {
        // 커밋된 픽스처와 REQUIRES_NEW로 커밋된 데이터가 다른 테스트에 남지 않도록 전체 테이블을 비운다 (H2)
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        List<String> tables = jdbcTemplate.queryForList(
            "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES "
                + "WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_TYPE = 'BASE TABLE'",
            String.class);
        tables.forEach(table -> jdbcTemplate.execute("TRUNCATE TABLE \"" + table + "\""));
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    /**
     * 상품그룹 + 포함그룹 - 제외그룹 구성의 MessageLog를 주어진 유저들의 Detail만 가진 채로 저장한다.
     */
    private MessageLog saveMessageLogWithDetails(LocalDateTime reserveTime, User... detailUsers) {
        MessageLog messageLog = MessageLog.messageLog(channel, messageType, reserveTime, "test");

        messageLog.updateGroupInfo(
            entitlementGroup,
            List.of(includeGroup.getId()),  // 포함 그룹
            List.of(exceptGroup.getId())    // 제외 그룹
        );

        for (User user : detailUsers) {
            messageLog.addMessageLogDetailInfo(
                MessageLogDetail.createLogDetail(messageLog, user, messageContent)
            );
        }

        return messageLogJpaRepository.save(messageLog);
    }

    /**
     * 오늘(KST) 0시를 UTC로 변환한 시각. 항상 현재보다 과거이면서 발송일은 오늘이다.
     */
    private static LocalDateTime todayKstStartInUtc() {
        return LocalDate.now(DateTimeUtils.KST)
            .atStartOfDay(DateTimeUtils.KST)
            .withZoneSameInstant(ZoneOffset.UTC)
            .toLocalDateTime();
    }

    @Test
    @DisplayName("상품그룹_포함그룹_제외그룹_유저조회_검증_테스트")
    void 상품그룹_포함그룹_제외그룹_유저조회_검증_테스트() {
        // given: user1만 포함 (user2, user4는 누락)
        MessageLog savedMessageLog = saveMessageLogWithDetails(
            LocalDateTime.now().plusDays(1), user1);

        // when: 누락 유저 추가 실행
        int addedCount = messageLogDetailService.addMissingDetailsBeforeSend(
            savedMessageLog.getId()
        );

        // then
        assertThat(addedCount).isEqualTo(2);  // user2(includeGroup), user4(신규)

        Set<Long> userIds = messageLogDetailQueryRepository
            .findUserIdsByMessageLogId(savedMessageLog.getId());

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
        // given: 모든 대상 유저 포함
        MessageLog savedMessageLog = saveMessageLogWithDetails(
            LocalDateTime.now().plusDays(1), user1, user2, user4);

        // when
        int addedCount = messageLogDetailService.addMissingDetailsBeforeSend(
            savedMessageLog.getId()
        );

        // then
        assertThat(addedCount).isZero();
    }

    @Test
    @DisplayName("카카오 로그는 reserveTime이 지났어도 발송일(KST) 당일이면 누락 유저를 추가한다")
    void 카카오_당일_reserveTime지남_누락유저_추가() {
        // given: reserveTime은 이미 지났지만 발송일은 오늘
        MessageLog savedMessageLog = saveMessageLogWithDetails(todayKstStartInUtc(), user1);

        // when
        int addedCount = messageLogDetailService.addMissingDetailsBeforeSend(
            savedMessageLog.getId()
        );

        // then
        assertThat(addedCount).isEqualTo(2);  // user2(includeGroup), user4(신규)
        assertThat(messageLogDetailQueryRepository.findUserIdsByMessageLogId(savedMessageLog.getId()))
            .containsExactlyInAnyOrder(user1.getId(), user2.getId(), user4.getId());
    }

    @Test
    @DisplayName("발송일(KST)이 지난 카카오 로그는 누락 유저가 있어도 예외 없이 0을 반환한다")
    void 발송일지남_누락유저있어도_스킵() {
        // given: 어제 로그, user4 누락
        MessageLog savedMessageLog = saveMessageLogWithDetails(
            LocalDateTime.now().minusDays(1), user1);

        // when
        int addedCount = messageLogDetailService.addMissingDetailsBeforeSend(
            savedMessageLog.getId()
        );

        // then: 추가되지 않고 기존 상세만 남는다
        assertThat(addedCount).isZero();
        assertThat(messageLogDetailQueryRepository.findUserIdsByMessageLogId(savedMessageLog.getId()))
            .containsExactly(user1.getId());
    }

    @Test
    @DisplayName("발송 트랜잭션 안에서 추가된 상세도 REQUIRES_NEW 상태 갱신에 반영된다")
    void 발송트랜잭션내_추가상세_상태갱신_반영() {
        // given: user4 누락
        MessageLog savedMessageLog = saveMessageLogWithDetails(
            LocalDateTime.now().plusDays(1), user1, user2);

        // when: 발송 흐름처럼 바깥 트랜잭션 안에서 누락 유저 추가 → 발송 결과 상태 갱신(REQUIRES_NEW)
        Long newDetailId = transactionTemplate.execute(status -> {
            messageLogDetailService.addMissingDetailsBeforeSend(savedMessageLog.getId());

            Long detailId = messageLogDetailJpaRepository.findAll().stream()
                .filter(d -> d.getUser().getId().equals(user4.getId()))
                .findFirst().orElseThrow().getId();

            messageLogDetailService.updateStatusByIds(List.of(detailId), ReservationStatus.COMPLETE);
            return detailId;
        });

        // then: 새 상세가 커밋된 뒤 갱신됐으므로 COMPLETE 상태여야 한다
        assertThat(messageLogDetailJpaRepository.findById(newDetailId).orElseThrow().getStatus())
            .isEqualTo(ReservationStatus.COMPLETE);
    }

    @Test
    @DisplayName("재활성 유저의 EXPIRED Detail은 PREPARE로 복구되고, 제외그룹 유저는 복구되지 않음")
    void 재활성유저_EXPIRED_복구_테스트() {
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

        // user1: 만료됐다가 재구매로 그룹에 복귀한 상태 (그룹 소속 + EXPIRED Detail)
        MessageLogDetail revivalTarget = MessageLogDetail.createLogDetail(
            messageLog, user1, messageContent);
        // user3: 제외그룹 소속 + EXPIRED Detail (복구되면 안 됨)
        MessageLogDetail exceptTarget = MessageLogDetail.createLogDetail(
            messageLog, user3, messageContent);
        messageLog.addMessageLogDetailInfo(revivalTarget);
        messageLog.addMessageLogDetailInfo(exceptTarget);
        messageLog.addMessageLogDetailInfo(
            MessageLogDetail.createLogDetail(messageLog, user2, messageContent));
        messageLog.addMessageLogDetailInfo(
            MessageLogDetail.createLogDetail(messageLog, user4, messageContent));

        MessageLog savedMessageLog = messageLogJpaRepository.save(messageLog);

        messageLogDetailQueryRepository.updateStatusByIds(
            List.of(revivalTarget.getId(), exceptTarget.getId()), ReservationStatus.EXPIRED);

        // when
        int addedCount = messageLogDetailService.addMissingDetailsBeforeSend(
            savedMessageLog.getId()
        );

        // then: 신규 추가 유저는 없고, 그룹에 남아있는 user1의 EXPIRED만 PREPARE로 복구된다
        assertThat(addedCount).isZero();
        assertThat(messageLogDetailJpaRepository.findById(revivalTarget.getId())
            .orElseThrow().getStatus()).isEqualTo(ReservationStatus.PREPARE);
        assertThat(messageLogDetailJpaRepository.findById(exceptTarget.getId())
            .orElseThrow().getStatus()).isEqualTo(ReservationStatus.EXPIRED);
    }

}
