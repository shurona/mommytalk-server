package com.shrona.mommytalk.message.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.config.JpaTestConfig;
import com.shrona.mommytalk.entitlement.domain.Entitlement;
import com.shrona.mommytalk.entitlement.domain.EntitlementType;
import com.shrona.mommytalk.entitlement.infrastructure.jpa.EntitlementJpaRepository;
import com.shrona.mommytalk.kakao.domain.KakaoUser;
import com.shrona.mommytalk.kakao.infrastructure.repository.jpa.KakaoUserJpaRepository;
import com.shrona.mommytalk.line.infrastructure.repository.jpa.ChannelJpaRepository;
import com.shrona.mommytalk.message.domain.MessageContent;
import com.shrona.mommytalk.message.domain.MessageLog;
import com.shrona.mommytalk.message.domain.MessageLogDetail;
import com.shrona.mommytalk.message.domain.type.ReservationStatus;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageContentJpaRepository;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageLogDetailJpaRepository;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageLogJpaRepository;
import com.shrona.mommytalk.message.infrastructure.repository.query.MessageLogDetailQueryRepositoryImpl;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import com.shrona.mommytalk.user.infrastructure.repository.jpa.UserJpaRepository;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Import({JpaTestConfig.class, MessageLogDetailQueryRepositoryImpl.class})
@DataJpaTest
class MessageLogDetailQueryRepositoryImplTest {

    @Autowired
    private MessageLogDetailQueryRepositoryImpl messageLogDetailQueryRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private KakaoUserJpaRepository kakaoUserJpaRepository;
    @Autowired
    private ChannelJpaRepository channelJpaRepository;
    @Autowired
    private MessageLogJpaRepository messageLogJpaRepository;
    @Autowired
    private MessageLogDetailJpaRepository messageLogDetailJpaRepository;
    @Autowired
    private MessageContentJpaRepository messageContentJpaRepository;
    @Autowired
    private EntitlementJpaRepository entitlementJpaRepository;
    @Autowired
    private TestEntityManager em;

    private MessageLog messageLog;
    private MessageLog otherLog;
    private MessageContent content;

    private MessageLogDetail prepare0900;   // PREPARE, 선호 09:00
    private MessageLogDetail prepare1000;   // PREPARE, 선호 10:00
    private MessageLogDetail complete0900;  // COMPLETE, 선호 09:00
    private MessageLogDetail noKakao0900;   // PREPARE, 카카오 미연동
    private MessageLogDetail noPhone0900;   // PREPARE, 전화번호 없음
    private MessageLogDetail otherLogDetail; // 다른 MessageLog의 PREPARE

    @BeforeEach
    void setUp() {
        Channel channel = channelJpaRepository.save(
            Channel.createChannel("kakaoChannel", "카카오 채널"));

        content = messageContentJpaRepository.save(
            MessageContent.of(null, "오늘의 영어 {아이이름}", 1, 1));

        messageLog = messageLogJpaRepository.save(
            MessageLog.messageLog(channel, null, LocalDateTime.now(), "test"));
        otherLog = messageLogJpaRepository.save(
            MessageLog.messageLog(channel, null, LocalDateTime.now(), "other"));

        User user0900 = createKakaoLinkedUser("010-1111-1111", LocalTime.of(9, 0));
        User user1000 = createKakaoLinkedUser("010-2222-2222", LocalTime.of(10, 0));
        User userComplete = createKakaoLinkedUser("010-3333-3333", LocalTime.of(9, 0));
        User userOtherLog = createKakaoLinkedUser("010-5555-5555", LocalTime.of(9, 0));

        // 카카오 미연동 유저
        User userNoKakao = userJpaRepository.save(
            User.createUser(new PhoneNumber("010-4444-4444")));
        userNoKakao.updatePreferredSendTime(LocalTime.of(9, 0));

        // 전화번호 없는 유저 (카카오만 연동)
        User userNoPhone = userJpaRepository.save(User.createUser(null));
        KakaoUser kakaoOnly = kakaoUserJpaRepository.save(
            KakaoUser.createKakaoUser("kakao-no-phone", userNoPhone));
        userNoPhone.updateKakaoUser(kakaoOnly);
        userNoPhone.updatePreferredSendTime(LocalTime.of(9, 0));

        prepare0900 = messageLogDetailJpaRepository.save(
            MessageLogDetail.createLogDetail(messageLog, user0900, content));
        prepare1000 = messageLogDetailJpaRepository.save(
            MessageLogDetail.createLogDetail(messageLog, user1000, content));
        // createLogDetailForLegacy는 COMPLETE 상태로 생성된다
        complete0900 = messageLogDetailJpaRepository.save(
            MessageLogDetail.createLogDetailForLegacy(messageLog, userComplete, content));
        noKakao0900 = messageLogDetailJpaRepository.save(
            MessageLogDetail.createLogDetail(messageLog, userNoKakao, content));
        noPhone0900 = messageLogDetailJpaRepository.save(
            MessageLogDetail.createLogDetail(messageLog, userNoPhone, content));
        otherLogDetail = messageLogDetailJpaRepository.save(
            MessageLogDetail.createLogDetail(otherLog, userOtherLog, content));
    }

    private User createKakaoLinkedUser(String phone, LocalTime preferredTime) {
        User user = userJpaRepository.save(User.createUser(new PhoneNumber(phone)));
        KakaoUser kakaoUser = kakaoUserJpaRepository.save(
            KakaoUser.createKakaoUser("kakao-" + phone, user));
        user.updateKakaoUser(kakaoUser);
        user.updatePreferredSendTime(preferredTime);
        return user;
    }

    @Test
    public void PREPARE_상태만_조회하고_COMPLETE는_제외_테스트() {
        // when
        List<MessageLogDetail> result = messageLogDetailQueryRepository
            .findMldListByStatusWithKakao(
                messageLog.getId(), List.of(ReservationStatus.PREPARE), null);

        // then: 중복 발송 방지의 핵심 조건
        List<Long> ids = result.stream().map(MessageLogDetail::getId).toList();
        assertThat(ids).contains(prepare0900.getId(), prepare1000.getId());
        assertThat(ids).doesNotContain(complete0900.getId());
    }

    @Test
    public void 선호_시간이_윈도우_이전인_유저만_조회_테스트() {
        // when: 윈도우 끝 09:25
        List<MessageLogDetail> result = messageLogDetailQueryRepository
            .findMldListByStatusWithKakao(
                messageLog.getId(), List.of(ReservationStatus.PREPARE), LocalTime.of(9, 25));

        // then: 09:00 유저 포함, 10:00 유저 제외
        List<Long> ids = result.stream().map(MessageLogDetail::getId).toList();
        assertThat(ids).contains(prepare0900.getId());
        assertThat(ids).doesNotContain(prepare1000.getId());
    }

    @Test
    public void 선호_시간이_윈도우_끝과_같으면_제외_테스트() {
        // when: 윈도우 끝 10:00 (lt 조건이므로 10:00 유저는 다음 윈도우 대상)
        List<MessageLogDetail> result = messageLogDetailQueryRepository
            .findMldListByStatusWithKakao(
                messageLog.getId(), List.of(ReservationStatus.PREPARE), LocalTime.of(10, 0));

        // then
        List<Long> ids = result.stream().map(MessageLogDetail::getId).toList();
        assertThat(ids).contains(prepare0900.getId());
        assertThat(ids).doesNotContain(prepare1000.getId());
    }

    @Test
    public void 시간_필터가_null이면_시간_무관_전체_조회_테스트() {
        // when: 수동 재전송 경로 (시간 필터 없음)
        List<MessageLogDetail> result = messageLogDetailQueryRepository
            .findMldListByStatusWithKakao(
                messageLog.getId(), List.of(ReservationStatus.PREPARE), null);

        // then
        List<Long> ids = result.stream().map(MessageLogDetail::getId).toList();
        assertThat(ids).contains(prepare0900.getId(), prepare1000.getId());
    }

    @Test
    public void 카카오_미연동_또는_전화번호_없는_유저는_제외_테스트() {
        // when
        List<MessageLogDetail> result = messageLogDetailQueryRepository
            .findMldListByStatusWithKakao(
                messageLog.getId(), List.of(ReservationStatus.PREPARE), null);

        // then
        List<Long> ids = result.stream().map(MessageLogDetail::getId).toList();
        assertThat(ids).doesNotContain(noKakao0900.getId(), noPhone0900.getId());
    }

    @Test
    public void 다른_MessageLog의_상세는_조회되지_않음_테스트() {
        // when
        List<MessageLogDetail> result = messageLogDetailQueryRepository
            .findMldListByStatusWithKakao(
                messageLog.getId(), List.of(ReservationStatus.PREPARE), null);

        // then
        List<Long> ids = result.stream().map(MessageLogDetail::getId).toList();
        assertThat(ids).doesNotContain(otherLogDetail.getId());
    }

    @Test
    public void 만료_유저의_미래_PREPARE_Detail만_EXPIRED로_전환_테스트() {
        // given
        Channel channel = channelJpaRepository.save(
            Channel.createChannel("expireChannel", "만료 테스트 채널"));
        Entitlement entitlement = entitlementJpaRepository.save(
            Entitlement.createEntitlement("마미톡", EntitlementType.MOMMYTALK));
        Entitlement otherEntitlement = entitlementJpaRepository.save(
            Entitlement.createEntitlement("마미보카", EntitlementType.MOMMYVOCA));

        User expiredUser = createKakaoLinkedUser("010-6666-6666", LocalTime.of(9, 0));
        User activeUser = createKakaoLinkedUser("010-7777-7777", LocalTime.of(9, 0));

        MessageLog futureLog = MessageLog.messageLog(
            channel, null, LocalDateTime.now().plusDays(1), "future");
        futureLog.updateMessageEntitlement(entitlement);
        MessageLog pastLog = MessageLog.messageLog(
            channel, null, LocalDateTime.now().minusDays(1), "past");
        pastLog.updateMessageEntitlement(entitlement);
        MessageLog otherEntitlementLog = MessageLog.messageLog(
            channel, null, LocalDateTime.now().plusDays(1), "otherEntitlement");
        otherEntitlementLog.updateMessageEntitlement(otherEntitlement);
        messageLogJpaRepository.saveAll(List.of(futureLog, pastLog, otherEntitlementLog));

        MessageLogDetail cancelTarget = messageLogDetailJpaRepository.save(
            MessageLogDetail.createLogDetail(futureLog, expiredUser, content));
        MessageLogDetail pastDetail = messageLogDetailJpaRepository.save(
            MessageLogDetail.createLogDetail(pastLog, expiredUser, content));
        MessageLogDetail otherEntitlementDetail = messageLogDetailJpaRepository.save(
            MessageLogDetail.createLogDetail(otherEntitlementLog, expiredUser, content));
        MessageLogDetail otherUserDetail = messageLogDetailJpaRepository.save(
            MessageLogDetail.createLogDetail(futureLog, activeUser, content));

        // when
        long expiredCount = messageLogDetailQueryRepository
            .expireFutureDetailsByUserAndEntitlement(
                expiredUser.getId(), entitlement.getId(), LocalDateTime.now());

        // 벌크 UPDATE는 영속성 컨텍스트를 우회하므로 초기화 후 재조회
        em.clear();

        // then: 만료 유저 + 해당 사용권 + 미래 예약 건만 EXPIRED로 전환된다
        assertThat(expiredCount).isEqualTo(1);
        assertThat(findStatusById(cancelTarget.getId())).isEqualTo(ReservationStatus.EXPIRED);
        assertThat(findStatusById(pastDetail.getId())).isEqualTo(ReservationStatus.PREPARE);
        assertThat(findStatusById(otherEntitlementDetail.getId()))
            .isEqualTo(ReservationStatus.PREPARE);
        assertThat(findStatusById(otherUserDetail.getId())).isEqualTo(ReservationStatus.PREPARE);
    }

    @Test
    public void 상세_목록_조회에서_EXPIRED는_제외_테스트() {
        // given: prepare0900을 만료 처리된 상태로 변경
        messageLogDetailQueryRepository.updateStatusByIds(
            List.of(prepare0900.getId()), ReservationStatus.EXPIRED);
        em.clear();

        // when
        Page<MessageLogDetail> result = messageLogDetailQueryRepository
            .findMessageLogDetailListByLogId(messageLog.getId(), PageRequest.of(0, 20));

        // then: EXPIRED 건은 목록과 전체 개수 모두에서 제외된다
        List<Long> ids = result.getContent().stream().map(MessageLogDetail::getId).toList();
        assertThat(ids).doesNotContain(prepare0900.getId());
        assertThat(ids).contains(
            prepare1000.getId(), complete0900.getId(), noKakao0900.getId(), noPhone0900.getId());
        assertThat(result.getTotalElements()).isEqualTo(4);
    }

    @Test
    public void 전체_취소는_EXPIRED_상태를_덮어쓰지_않음_테스트() {
        // given: prepare0900을 만료 처리된 상태로 변경
        messageLogDetailQueryRepository.updateStatusByIds(
            List.of(prepare0900.getId()), ReservationStatus.EXPIRED);

        // when: 전체 취소 실행
        messageLogDetailQueryRepository.cancelDetailByLogId(messageLog.getId());
        em.clear();

        // then: EXPIRED와 COMPLETE는 보존되고, 나머지 PREPARE만 CANCEL로 변경된다
        assertThat(findStatusById(prepare0900.getId())).isEqualTo(ReservationStatus.EXPIRED);
        assertThat(findStatusById(complete0900.getId())).isEqualTo(ReservationStatus.COMPLETE);
        assertThat(findStatusById(prepare1000.getId())).isEqualTo(ReservationStatus.CANCEL);
    }

    private ReservationStatus findStatusById(Long detailId) {
        return messageLogDetailJpaRepository.findAll().stream()
            .filter(detail -> detail.getId().equals(detailId))
            .findFirst()
            .orElseThrow()
            .getStatus();
    }
}
