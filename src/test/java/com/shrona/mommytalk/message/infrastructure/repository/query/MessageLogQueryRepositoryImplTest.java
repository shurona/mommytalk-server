package com.shrona.mommytalk.message.infrastructure.repository.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.config.JpaTestConfig;
import com.shrona.mommytalk.line.infrastructure.repository.jpa.ChannelJpaRepository;
import com.shrona.mommytalk.message.domain.MessageLog;
import com.shrona.mommytalk.message.domain.MessageLogDetail;
import com.shrona.mommytalk.message.domain.type.ReservationStatus;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageLogJpaRepository;
import com.shrona.mommytalk.message.presentation.dtos.response.MessageLogResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Import({JpaTestConfig.class, MessageQueryRepositoryImpl.class,
    MessageLogQueryRepositoryImpl.class, MessageLogDetailQueryRepositoryImpl.class})
@DataJpaTest
class MessageLogQueryRepositoryImplTest {

    @Autowired
    private MessageQueryRepositoryImpl messageQueryRepository;

    @Autowired
    private MessageLogQueryRepositoryImpl messageLogQueryRepository;

    @Autowired
    private MessageLogDetailQueryRepositoryImpl messageLogDetailQueryRepository;

    @Autowired
    private MessageLogJpaRepository messageLogRepository;

    @Autowired
    private ChannelJpaRepository channelJpaRepository;

    private Channel channel;

    @BeforeEach
    public void setUp() {

        channel = channelJpaRepository.save(
            Channel.createChannel("TestChannel", "테스트 채널")
        );


    }

    @Test
    public void 취소_여부_조회() {

        // given
        MessageLog hello = MessageLog.messageLog(channel, null, LocalDateTime.now().plusHours(5),
            "yahoo");
        MessageLog cancel = MessageLog.messageLog(channel, null, LocalDateTime.now().plusHours(5),
            "yahoo");

        hello.addMessageLogDetailInfo(MessageLogDetail.createLogDetail(hello, null, null));
        cancel.addMessageLogDetailInfo(MessageLogDetail.createLogDetail(cancel, null, null));

        cancel.cancelMessageLog();

        List<MessageLog> messageLogs = messageLogRepository.saveAll(List.of(hello, cancel));

        // when
        List<MessageLog> messageByIds = messageQueryRepository.findMessageByIds(
            messageLogs.stream().map(MessageLog::getId).toList()
        );
        List<MessageLog> messageByDate = messageQueryRepository.findAllByReservedMessageBeforeDate(
            LocalDateTime.now()
        );

        // then
        assertThat(messageByIds.size()).isEqualTo(1);
        assertThat(messageByDate.size()).isEqualTo(1);

    }

    @Test
    public void 유저_단위_취소는_대표_상태에_영향을_주지_않는다() {

        // given
        MessageLog messageLog = MessageLog.messageLog(
            channel, null, LocalDateTime.now().plusHours(5), "yahoo");
        MessageLogDetail cancelTarget = MessageLogDetail.createLogDetail(messageLog, null, null);
        MessageLogDetail remainTarget = MessageLogDetail.createLogDetail(messageLog, null, null);
        messageLog.addMessageLogDetailInfo(cancelTarget);
        messageLog.addMessageLogDetailInfo(remainTarget);
        messageLogRepository.save(messageLog);

        // 특정 유저의 Detail만 취소
        messageLogDetailQueryRepository.updateStatusByIds(
            List.of(cancelTarget.getId()), ReservationStatus.CANCEL);

        // when
        Page<MessageLogResponseDto> result = messageLogQueryRepository
            .findMessageLogsByChannel(channel.getId(), PageRequest.of(0, 20));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).status()).isEqualTo("PREPARE");
        assertThat(result.getContent().get(0).totalCount()).isEqualTo(2);
    }

    @Test
    public void 만료_EXPIRED_Detail은_목록_카운트에서_제외된다() {

        // given
        MessageLog messageLog = MessageLog.messageLog(
            channel, null, LocalDateTime.now().plusHours(5), "yahoo");
        MessageLogDetail expiredTarget = MessageLogDetail.createLogDetail(messageLog, null, null);
        MessageLogDetail remainTarget = MessageLogDetail.createLogDetail(messageLog, null, null);
        messageLog.addMessageLogDetailInfo(expiredTarget);
        messageLog.addMessageLogDetailInfo(remainTarget);
        messageLogRepository.save(messageLog);

        // 사용권 만료로 EXPIRED 처리
        messageLogDetailQueryRepository.updateStatusByIds(
            List.of(expiredTarget.getId()), ReservationStatus.EXPIRED);

        // when
        Page<MessageLogResponseDto> result = messageLogQueryRepository
            .findMessageLogsByChannel(channel.getId(), PageRequest.of(0, 20));

        // then: EXPIRED는 totalCount에 포함되지 않고, 대표 상태에도 영향 없다
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).status()).isEqualTo("PREPARE");
        assertThat(result.getContent().get(0).totalCount()).isEqualTo(1);
    }

    @Test
    public void 전체_취소된_로그는_CANCEL_상태로_표시된다() {

        // given
        MessageLog messageLog = MessageLog.messageLog(
            channel, null, LocalDateTime.now().plusHours(5), "yahoo");
        messageLog.addMessageLogDetailInfo(
            MessageLogDetail.createLogDetail(messageLog, null, null));
        messageLog.cancelMessageLog();
        messageLogRepository.save(messageLog);

        messageLogDetailQueryRepository.cancelDetailByLogId(messageLog.getId());

        // when
        Page<MessageLogResponseDto> result = messageLogQueryRepository
            .findMessageLogsByChannel(channel.getId(), PageRequest.of(0, 20));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).status()).isEqualTo("CANCEL");
    }

}