package com.shrona.mommytalk.message.infrastructure.repository.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.config.JpaTestConfig;
import com.shrona.mommytalk.line.infrastructure.repository.jpa.ChannelJpaRepository;
import com.shrona.mommytalk.message.domain.MessageLog;
import com.shrona.mommytalk.message.domain.MessageLogDetail;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageLogJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import({JpaTestConfig.class, MessageQueryRepositoryImpl.class})
@DataJpaTest
class MessageLogQueryRepositoryImplTest {

    @Autowired
    private MessageQueryRepositoryImpl messageQueryRepository;

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

}