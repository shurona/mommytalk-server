package com.shrona.mommytalk.message.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.channel.domain.ChannelPlatform;
import com.shrona.mommytalk.config.JpaTestConfig;
import com.shrona.mommytalk.line.infrastructure.repository.jpa.ChannelJpaRepository;
import com.shrona.mommytalk.message.domain.MessageContent;
import com.shrona.mommytalk.message.domain.MessageLog;
import com.shrona.mommytalk.message.domain.MessageLogDetail;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageContentJpaRepository;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageLogDetailJpaRepository;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageLogJpaRepository;
import com.shrona.mommytalk.message.infrastructure.repository.query.MessageQueryRepositoryImpl;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import com.shrona.mommytalk.user.infrastructure.repository.jpa.UserJpaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import({JpaTestConfig.class, MessageQueryRepositoryImpl.class})
@DataJpaTest
class MessageQueryRepositoryImplTest {

    @Autowired
    private MessageQueryRepositoryImpl messageQueryRepository;

    @Autowired
    private ChannelJpaRepository channelJpaRepository;
    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private MessageLogJpaRepository messageLogJpaRepository;
    @Autowired
    private MessageLogDetailJpaRepository messageLogDetailJpaRepository;
    @Autowired
    private MessageContentJpaRepository messageContentJpaRepository;

    private LocalDateTime startOfToday;
    private LocalDateTime endOfToday;

    private MessageLog kakaoLogToday;      // 오늘 + PREPARE 존재 → 조회 대상
    private MessageLog kakaoLogYesterday;  // 어제 → 범위 밖
    private MessageLog kakaoLogComplete;   // 오늘 + 전부 COMPLETE → 제외
    private MessageLog kakaoLogCancelled;  // 오늘 + 취소됨 → 제외
    private MessageLog lineLogToday;       // 오늘 + LINE 채널 → 제외

    @BeforeEach
    void setUp() {
        startOfToday = LocalDate.now().atStartOfDay();
        endOfToday = startOfToday.plusDays(1);

        Channel kakaoChannel = Channel.createChannel("kakao", "카카오 채널");
        kakaoChannel.updateChannelPlatform(ChannelPlatform.KAKAO);
        channelJpaRepository.save(kakaoChannel);

        Channel lineChannel = Channel.createChannel("line", "라인 채널");
        lineChannel.updateChannelPlatform(ChannelPlatform.LINE);
        channelJpaRepository.save(lineChannel);

        MessageContent content = messageContentJpaRepository.save(
            MessageContent.of(null, "오늘의 영어", 1, 1));

        User user = userJpaRepository.save(
            User.createUser(new PhoneNumber("010-1111-1111")));

        LocalDateTime now = LocalDateTime.now();

        kakaoLogToday = saveLogWithDetail(kakaoChannel, now, user, content, false, false);
        kakaoLogYesterday = saveLogWithDetail(
            kakaoChannel, now.minusDays(1), user, content, false, false);
        kakaoLogComplete = saveLogWithDetail(kakaoChannel, now, user, content, true, false);
        kakaoLogCancelled = saveLogWithDetail(kakaoChannel, now, user, content, false, true);
        lineLogToday = saveLogWithDetail(lineChannel, now, user, content, false, false);
    }

    private MessageLog saveLogWithDetail(
        Channel channel, LocalDateTime reserveTime, User user, MessageContent content,
        boolean detailComplete, boolean cancelled) {

        MessageLog log = MessageLog.messageLog(channel, null, reserveTime, "test");
        if (cancelled) {
            log.cancelMessageLog();
        }
        messageLogJpaRepository.save(log);

        // createLogDetailForLegacy는 COMPLETE, createLogDetail은 PREPARE 상태로 생성
        MessageLogDetail detail = detailComplete
            ? MessageLogDetail.createLogDetailForLegacy(log, user, content)
            : MessageLogDetail.createLogDetail(log, user, content);
        messageLogDetailJpaRepository.save(detail);

        return log;
    }

    @Test
    public void 오늘_범위의_카카오_로그만_조회_테스트() {
        // when
        List<MessageLog> result = messageQueryRepository
            .findKakaoLogsByReserveTimeRange(startOfToday, endOfToday);

        // then
        List<Long> ids = result.stream().map(MessageLog::getId).toList();
        assertThat(ids).contains(kakaoLogToday.getId());
        assertThat(ids).doesNotContain(kakaoLogYesterday.getId());
    }

    @Test
    public void PREPARE_상세가_없는_로그는_제외_테스트() {
        // when
        List<MessageLog> result = messageQueryRepository
            .findKakaoLogsByReserveTimeRange(startOfToday, endOfToday);

        // then: 전부 접수 완료된 로그는 다시 폴링 대상이 되지 않는다
        List<Long> ids = result.stream().map(MessageLog::getId).toList();
        assertThat(ids).doesNotContain(kakaoLogComplete.getId());
    }

    @Test
    public void 취소된_로그는_제외_테스트() {
        // when
        List<MessageLog> result = messageQueryRepository
            .findKakaoLogsByReserveTimeRange(startOfToday, endOfToday);

        // then
        List<Long> ids = result.stream().map(MessageLog::getId).toList();
        assertThat(ids).doesNotContain(kakaoLogCancelled.getId());
    }

    @Test
    public void 라인_채널_로그는_제외_테스트() {
        // when
        List<MessageLog> result = messageQueryRepository
            .findKakaoLogsByReserveTimeRange(startOfToday, endOfToday);

        // then
        List<Long> ids = result.stream().map(MessageLog::getId).toList();
        assertThat(ids).doesNotContain(lineLogToday.getId());
    }
}
