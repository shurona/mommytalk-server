package com.shrona.mommytalk.message.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.channel.domain.ChannelPlatform;
import com.shrona.mommytalk.message.application.MessageContentService;
import com.shrona.mommytalk.message.domain.MessageContent;
import com.shrona.mommytalk.message.domain.MessageLog;
import com.shrona.mommytalk.message.domain.MessageLogDetail;
import com.shrona.mommytalk.message.domain.MessageType;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MessageLogDetailDomainServiceTest {

    @Mock
    private MessageContentService messageContentService;

    @InjectMocks
    private MessageLogDetailDomainService domainService;

    private MessageLog messageLog;
    private MessageType messageType;
    private Channel channel;
    private List<User> users;
    private Map<String, MessageContent> levelMap;

    @BeforeEach
    void setUp() {
        // ✅ Channel 생성 (정적 팩토리 메서드)
        channel = Channel.createChannel("테스트채널", "테스트 설명");
        ReflectionTestUtils.setField(channel, "id", 1L);
        channel.updateChannelPlatform(ChannelPlatform.KAKAO);

        // ✅ MessageType 생성 (정적 팩토리 메서드)
        messageType = MessageType.of(
            "테스트 주제",
            "테스트 컨텍스트",
            LocalDate.now().plusDays(1),
            channel
        );
        ReflectionTestUtils.setField(messageType, "id", 1L);

        // ✅ MessageLog 생성 (정적 팩토리 메서드)
        messageLog = MessageLog.messageLog(
            channel,
            messageType,
            LocalDateTime.now().plusDays(1),
            "test"
        );

        // ✅ User 생성 (정적 팩토리 메서드)
        User user1 = User.createUser(new PhoneNumber("01012345678"));
        ReflectionTestUtils.setField(user1, "id", 1L);
        ReflectionTestUtils.setField(user1, "childLevel", 2);
        ReflectionTestUtils.setField(user1, "userLevel", 2);

        User user2 = User.createUser(new PhoneNumber("01087654321"));
        ReflectionTestUtils.setField(user2, "id", 2L);
        ReflectionTestUtils.setField(user2, "childLevel", 1);
        ReflectionTestUtils.setField(user2, "userLevel", 3);

        users = List.of(user1, user2);

        // ✅ MessageContent 생성 (정적 팩토리 메서드)
        MessageContent content1 = MessageContent.of(messageType, "메시지1", 2, 2);
        ReflectionTestUtils.setField(content1, "id", 1L);

        MessageContent content2 = MessageContent.of(messageType, "메시지2", 1, 3);
        ReflectionTestUtils.setField(content2, "id", 2L);

        // 레벨 매핑
        levelMap = Map.of(
            "2_2", content1,
            "3_1", content2
        );
    }

    @Test
    @DisplayName("유저 목록에 대한 MessageLogDetail 생성 성공")
    void createDetailsForUsers_성공() {
        // given
        given(messageContentService.groupMessageContentByLevel(any()))
            .willReturn(levelMap);

        // when
        List<MessageLogDetail> result = domainService.createDetailsForUsers(messageLog, users);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUser().getId()).isEqualTo(1L);
        assertThat(result.get(0).getMessageContent().getId()).isEqualTo(1L);
        assertThat(result.get(1).getUser().getId()).isEqualTo(2L);
        assertThat(result.get(1).getMessageContent().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("MessageContent가 없는 유저는 제외")
    void createDetailsForUsers_컨텐츠없는유저제외() {
        // given
        User user3 = User.createUser(new PhoneNumber("01099999999"));
        ReflectionTestUtils.setField(user3, "id", 3L);
        ReflectionTestUtils.setField(user3, "childLevel", 3);
        ReflectionTestUtils.setField(user3, "userLevel", 3);

        List<User> usersWithUnmatched = List.of(users.get(0), user3);

        given(messageContentService.groupMessageContentByLevel(any()))
            .willReturn(levelMap);

        // when
        List<MessageLogDetail> result = domainService.createDetailsForUsers(
            messageLog, usersWithUnmatched);

        // then
        assertThat(result).hasSize(1);  // user3는 제외됨
        assertThat(result.get(0).getUser().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("MessageLog가 변경 불가능한 상태면 예외 발생")
    void createDetailsForUsers_변경불가상태_예외발생() {
        // given
        MessageLog pastMessageLog = MessageLog.messageLog(
            channel,
            messageType,
            LocalDateTime.now().minusDays(1),  // 과거 시간
            "test"
        );

        // when & then
        assertThatThrownBy(() ->
            domainService.createDetailsForUsers(pastMessageLog, users))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("MessageLog 상태가 변경 불가능합니다.");
    }

    @Test
    @DisplayName("취소된 MessageLog는 변경 불가")
    void createDetailsForUsers_취소된MessageLog_예외발생() {
        // given
        messageLog.cancelMessageLog();  // 취소 상태로 변경
        
        // when & then
        assertThatThrownBy(() ->
            domainService.createDetailsForUsers(messageLog, users))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("MessageLog 상태가 변경 불가능합니다.");
    }

    @Test
    @DisplayName("빈 유저 목록이면 빈 리스트 반환")
    void createDetailsForUsers_빈유저목록() {
        // given
        given(messageContentService.groupMessageContentByLevel(any()))
            .willReturn(levelMap);

        // when
        List<MessageLogDetail> result = domainService.createDetailsForUsers(
            messageLog, List.of());

        // then
        Assertions.assertThat(result).isEmpty();
    }

}