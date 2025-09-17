package com.shrona.mommytalk.linehook.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.line.domain.ChannelLineUser;
import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.line.infrastructure.ChannelJpaRepository;
import com.shrona.mommytalk.line.infrastructure.ChannelLineUserJpaRepository;
import com.shrona.mommytalk.line.infrastructure.LineUserJpaRepository;
import com.shrona.mommytalk.message.common.utils.MessageUtils;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import com.shrona.mommytalk.user.infrastructure.UserJpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ChannelHookServiceImplTest {

    @Autowired
    private ChannelHookServiceImpl channelHookService;
    @Autowired
    private UserJpaRepository userRepository;
    @Autowired
    private LineUserJpaRepository lineUserRepository;
    @Autowired
    private ChannelJpaRepository channelJpaRepository;
    @Autowired
    private ChannelLineUserJpaRepository channelLineUserRepository;

    @MockitoBean
    private MessageUtils messageUtils;


    private LineUser lineUserOne;
    private LineUser lineUserTwo;
    private LineUser lineUserThree;
    private ChannelLineUser channelLineUserOne;
    private ChannelLineUser channelLineUserTwo;
    private User userBlankLineUser;
    private User userWithLineUser;
    private Channel channel;

    @BeforeEach
    public void beforeEach() {
        channel = Channel.createChannel("name", "");
        channelJpaRepository.save(channel);

        lineUserOne = lineUserRepository.save(LineUser.createLineUser("lineOne"));
        lineUserTwo = lineUserRepository.save(LineUser.createLineUser("lineTwo"));
        lineUserThree = lineUserRepository.save(
            LineUser.createLineUser("lineThree"));  // 추가!

        // User 생성 (LineUser와 연결)
        User userOne = userRepository.save(User.createUserWithLine(null, lineUserOne));
        User userTwo = userRepository.save(User.createUserWithLine(null, lineUserTwo));

        // 채널 정보 생성
        channelLineUserOne = channelLineUserRepository.save(
            ChannelLineUser.create(channel, lineUserOne));
        channelLineUserTwo = channelLineUserRepository.save(
            ChannelLineUser.create(channel, lineUserTwo));

        userBlankLineUser = userRepository.save(User.createUser(new PhoneNumber("010-1234-1234")));
        userWithLineUser = userRepository.save(
            User.createUserWithLine(new PhoneNumber("010-1234-1235"), lineUserThree));


    }

    @Nested
    class 통합_테스트 {

        @Test
        public void 라인_훅_전체_플로우_테스트() {
            // given
            Long channelId = channel.getId();
            String newLineId = "newLineUser123";
            String validPhoneNumber = "010-9876-5432";
            String invalidMessage = "일반 메시지";

            // when & then - 1단계: 일반 메시지 저장 (휴대전화 형식 아님)
            boolean result1 = channelHookService.saveLineMessage(channelId, newLineId,
                invalidMessage);
            assertThat(result1).isFalse(); // 휴대전화 형식이 아니므로 false

            // when & then - 2단계: 휴대전화 형식 메시지 저장
            boolean result2 = channelHookService.saveLineMessage(channelId, newLineId,
                validPhoneNumber);
            assertThat(result2).isTrue(); // 휴대전화 형식이므로 매칭 성공

            // when & then - 3단계: 같은 번호로 다시 시도 (이미 휴대전화가 등록된 상태)
            boolean result3 = channelHookService.saveLineMessage(channelId, newLineId,
                validPhoneNumber);
            assertThat(result3).isFalse(); // 이미 휴대전화가 등록되어 있으므로 false

            Optional<LineUser> byLineId = lineUserRepository.findByLineId(newLineId);
            Optional<User> byLineUser = userRepository.findByLineUser(byLineId.get());
            assertThat(byLineUser.get().getPhoneNumber().getPhoneNumber()).isEqualTo(
                validPhoneNumber);
        }

        @Test
        public void 팔로우_언팔로우_테스트() {
            // given
            Long channelId = channel.getId();
            String testLineId = "testFollowLineId";

            // when & then - 팔로우 테스트
            ChannelLineUser followResult = channelHookService.followLineUserByLineId(channelId,
                testLineId);
            assertThat(followResult).isNotNull();
            assertThat(followResult.getChannel().getId()).isEqualTo(channelId);
            assertThat(followResult.getLineUser().getLineId()).isEqualTo(testLineId);

            // 팔로우 후 LineUser가 생성되었는지 확인
            Optional<LineUser> createdLineUser = lineUserRepository.findByLineId(testLineId);
            assertThat(createdLineUser).isPresent();
            assertThat(createdLineUser.get().getLineId()).isEqualTo(testLineId);

            // when
            channelHookService.unfollowLineUserByLineId(channelId, testLineId);

            // then - 언팔로우 후 ChannelLineUser가 False로 변경되었는지 확인한다.
            Optional<ChannelLineUser> connectionAfterUnfollow =
                channelLineUserRepository.findByChannelAndLineUser(channel,
                    followResult.getLineUser());

            assertThat(connectionAfterUnfollow.get().isFollow()).isFalse();
        }
    }

    @Nested
    class 휴대전화_매칭_테스트 {

        @Test
        public void 기존_휴대전화_User에_LineUser_연결() {
            // given
            String existingPhoneNumber = "010-5555-5555";
            User existingUser = userRepository.save(
                User.createUser(new PhoneNumber(existingPhoneNumber)));
            LineUser newLineUser = lineUserRepository.save(
                LineUser.createLineUser("newLineForExisting"));

            // when
            boolean result = channelHookService.validatePhoneAndMatchUser(existingPhoneNumber,
                newLineUser);

            // then
            assertThat(result).isTrue();
            User updatedUser = userRepository.findByPhoneNumber(
                PhoneNumber.changeWithoutError(existingPhoneNumber)).get();
            assertThat(updatedUser.getLineUser().getLineId()).isEqualTo("newLineForExisting");
        }

        @Test
        public void 신규_휴대전화_번호로_새_User_생성() {
            // given
            String newPhoneNumber = "010-9999-8888";
            LineUser newLineUser = lineUserRepository.save(
                LineUser.createLineUser("newTestLineUser"));

            // 새로운 휴대전화 번호가 기존에 없는지 확인
            Optional<User> beforeTest = userRepository.findByPhoneNumber(
                PhoneNumber.changeWithoutError(newPhoneNumber));
            assertThat(beforeTest).isEmpty();

            // when
            boolean result = channelHookService.validatePhoneAndMatchUser(newPhoneNumber,
                newLineUser);

            // then
            assertThat(result).isTrue();

            // 새로운 User가 생성되었는지 확인
            Optional<User> createdUser = userRepository.findByPhoneNumber(
                PhoneNumber.changeWithoutError(newPhoneNumber));
            assertThat(createdUser).isPresent();
            assertThat(createdUser.get().getLineUser().getLineId()).isEqualTo("newTestLineUser");
            assertThat(createdUser.get().getPhoneNumber().getPhoneNumber()).isEqualTo(
                newPhoneNumber);
        }

        @Test
        public void LineUser_연결된_User에_휴대전화_추가() {
            // given
            LineUser existingLineUser = lineUserRepository.save(
                LineUser.createLineUser("existingLineUser"));
            User userWithoutPhone = userRepository.save(
                User.createUserWithLine(null, existingLineUser));
            String newPhoneNumber = "010-7777-7777";

            // 초기 상태 확인: User는 존재하지만 휴대전화가 없음
            assertThat(userWithoutPhone.getLineUser().getLineId()).isEqualTo("existingLineUser");
            assertThat(userWithoutPhone.getPhoneNumber()).isNull();

            // when
            boolean result = channelHookService.validatePhoneAndMatchUser(newPhoneNumber,
                existingLineUser);

            // then
            assertThat(result).isTrue();

            // 기존 User에 휴대전화가 추가되었는지 확인
            User updatedUser = userRepository.findByLineUser(existingLineUser).get();
            assertThat(updatedUser.getPhoneNumber()).isNotNull();
            assertThat(updatedUser.getPhoneNumber().getPhoneNumber()).isEqualTo(newPhoneNumber);
            assertThat(updatedUser.getLineUser().getLineId()).isEqualTo("existingLineUser");
        }

        @Test
        public void 이미_다른_LineUser가_연결된_휴대전화_입력시_연결_거부() {
            // given
            // 테스트할 유저가 lineUser가 등록되어 있는 지 확인
            assertThat(userWithLineUser.getLineUser().getLineId())
                .isEqualTo(lineUserThree.getLineId());

            // when
            boolean result = channelHookService.validatePhoneAndMatchUser("010-1234-1235",
                lineUserTwo);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    class 환영_메시지_발송_테스트 {

        @Test
        public void 성공_휴대전화_입력_시_메시지_발송() {
            // given
            doNothing().when(messageUtils).registerSingleTask(
                any(Channel.class), any(LineUser.class), any(), any(LocalDateTime.class));

            String phoneNumber = "010-1234-1234";

            // when
            channelHookService.sendLineMessageAfterSuccess(channel.getId(), lineUserOne.getLineId(),
                phoneNumber);

            // then
            verify(messageUtils).registerSingleTask(
                any(Channel.class), any(LineUser.class), any(), any(LocalDateTime.class));
        }

        @Test
        public void 잘못된_휴대전화_입력_시_메시지_발송_안함() {
            // given
            String wrongPhoneNumber = "010-12-1234";

            // when
            channelHookService.sendLineMessageAfterSuccess(channel.getId(), lineUserOne.getLineId(),
                wrongPhoneNumber);

            // then
            // 잘못된 휴대전화 형식이므로 registerSingleTask가 호출되지 않아야 함
            verify(messageUtils, never()).registerSingleTask(
                any(Channel.class), any(LineUser.class), any(), any(LocalDateTime.class));
        }
    }

}