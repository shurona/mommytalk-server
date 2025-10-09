package com.shrona.mommytalk.openai.infrastructure.repository.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.config.JpaTestConfig;
import com.shrona.mommytalk.line.infrastructure.repository.jpa.ChannelJpaRepository;
import com.shrona.mommytalk.openai.domain.MessagePrompt;
import com.shrona.mommytalk.openai.domain.type.PromptType;
import com.shrona.mommytalk.openai.infrastructure.repository.dto.PromptHistoryDto;
import com.shrona.mommytalk.openai.infrastructure.repository.jpa.MessagePromptJpaRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import({JpaTestConfig.class, MessagePromptQueryRepositoryImpl.class})
@DataJpaTest
public class MessagePromptQueryRepositoryImplTest {

    @Autowired
    private MessagePromptQueryRepositoryImpl messagePromptQueryRepository;

    @Autowired
    private MessagePromptJpaRepository messagePromptJpaRepository;

    @Autowired
    private ChannelJpaRepository channelJpaRepository;

    private Channel channel;

    @BeforeEach
    public void setUp() {

        channel = channelJpaRepository.save(
            Channel.createChannel("TestChannel", "테스트 채널")
        );

        MessagePrompt selectBasic = MessagePrompt.of(channel, "promptBasic", "label",
            PromptType.BASIC);
        selectBasic.registerPrompt();
        MessagePrompt selectAd = MessagePrompt.of(channel, "prompt", "label",
            PromptType.ADVANCE);
        selectAd.registerPrompt();
        messagePromptJpaRepository.saveAll(
            List.of(
                selectAd, selectBasic
            )
        );

        MessagePrompt oneBasic = MessagePrompt.of(channel, "oneBa", "oneBa", PromptType.BASIC);
        oneBasic.disablePrompt();
        MessagePrompt twoBasic = MessagePrompt.of(channel, "twoBa", "twoBa", PromptType.BASIC);
        twoBasic.disablePrompt();
        MessagePrompt oneAdvance = MessagePrompt.of(channel, "oneAd", "oneAd", PromptType.ADVANCE);
        oneAdvance.disablePrompt();
        MessagePrompt twoAdvance = MessagePrompt.of(channel, "twoAd", "twoAd", PromptType.ADVANCE);
        twoAdvance.disablePrompt();
        MessagePrompt threeAdvance = MessagePrompt.of(channel, "twoAd", "twoAd",
            PromptType.ADVANCE);
        threeAdvance.disablePrompt();

        messagePromptJpaRepository.saveAll(
            List.of(
                oneBasic, twoBasic, oneAdvance, twoAdvance, threeAdvance
            )
        );


    }


    @Test
    public void 선택된_프롬프트목록_단일_조회() {
        MessagePrompt basicAndSelectedPrompt = messagePromptQueryRepository
            .findSelectedPromptByChannelAndType(channel, PromptType.BASIC);

        assertThat(basicAndSelectedPrompt.getPrompt()).isEqualTo("promptBasic");
    }

    @Test
    public void 선택된_프롬프트목록_조회() {
        List<MessagePrompt> selectedPromptList = messagePromptQueryRepository
            .findSelectedPromptList(channel);

        assertThat(selectedPromptList.size()).isEqualTo(2);
        for (MessagePrompt messagePrompt : selectedPromptList) {
            assertThat(messagePrompt.getSelected()).isTrue();
        }
    }

    @Test
    public void History_조회() {
        List<PromptHistoryDto> basicPromptList = messagePromptQueryRepository.findPromptHistoryList(
            channel, PromptType.BASIC);
        List<PromptHistoryDto> advancePromptList = messagePromptQueryRepository.findPromptHistoryList(
            channel, PromptType.ADVANCE);

        assertThat(basicPromptList.size()).isEqualTo(3);
        assertThat(advancePromptList.size()).isEqualTo(4);
    }

}