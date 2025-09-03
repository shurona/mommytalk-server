package com.shrona.mommytalk.linehook.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.shrona.mommytalk.line.application.utils.MessageUtils;
import com.shrona.mommytalk.line.domain.Channel;
import com.shrona.mommytalk.line.domain.ChannelLineUser;
import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.line.infrastructure.ChannelJpaRepository;
import com.shrona.mommytalk.line.infrastructure.ChannelLineUserJpaRepository;
import com.shrona.mommytalk.line.infrastructure.LineUserJpaRepository;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import com.shrona.mommytalk.user.infrastructure.UserJpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
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

    @Test
    public void 라인_훅_통합_테스트() {
        // given
        Long channelId = channel.getId();
        String newLineId = "newLineUser123";
        String validPhoneNumber = "010-9876-5432";
        String invalidMessage = "일반 메시지";

        // when & then - 1단계: 일반 메시지 저장 (휴대전화 형식 아님)
        boolean result1 = channelHookService.saveLineMessage(channelId, newLineId, invalidMessage);
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

        assertThat(byLineUser.get().getPhoneNumber().getPhoneNumber()).isEqualTo(validPhoneNumber);

    }

    @Test
    public void 라인_훅_팔로우_언팔로우_통합_테스트() {
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

        // when & then - 언팔로우 테스트
        channelHookService.unfollowLineUserByLineId(channelId, testLineId);

        // 언팔로우 후 ChannelLineUser가 삭제되었는지 확인
        Optional<ChannelLineUser> connectionAfterUnfollow =
            channelLineUserRepository.findByChannelAndLineUser(channel, followResult.getLineUser());

        assertThat(connectionAfterUnfollow.get().isFollow()).isFalse();
    }

    @Test
    public void 휴대전화_입력시_비어있는_경우_조회_및_변경() {
        // given
        assertThat(userBlankLineUser.getLineUser()).isNull();
        String givenPhoneNumber = "010-1234-9999";

        // when
        boolean b = channelHookService.validatePhoneAndMatchUser(givenPhoneNumber,
            lineUserTwo);

        userBlankLineUser = userRepository.findByPhoneNumber(
            PhoneNumber.changeWithoutError(givenPhoneNumber)).get();

        // then
        assertThat(userBlankLineUser.getLineUser().getLineId()).isEqualTo(lineUserTwo.getLineId());
        assertThat(b).isTrue();
    }

    @Test
    public void 휴대전화_입력시_이미있는_경우_미_변경() {
        // given
        // 테스트할 유저가 lineUser가 등록되어 있는 지 확인
        assertThat(userWithLineUser.getLineUser().getLineId())
            .isEqualTo(lineUserThree.getLineId());

        // when
        boolean b = channelHookService.validatePhoneAndMatchUser("010-1234-1235",
            lineUserTwo);
        // then
        assertThat(b).isFalse();
    }

    @Test
    public void 성공_휴대전화_입력_시_단일전송_로직_테스트() {
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
    public void 잘못된_휴대전화_입력_시_단일전송_로직_테스트() {
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