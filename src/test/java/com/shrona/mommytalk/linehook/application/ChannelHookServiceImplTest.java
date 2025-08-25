package com.shrona.mommytalk.linehook.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shrona.mommytalk.line.application.LineService;
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

    @MockitoBean
    private LineService lineService;


    private LineUser lineUserOne;
    private LineUser lineUserTwo;
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

        // 채널 정보 생성
        channelLineUserOne = channelLineUserRepository.save(
            ChannelLineUser.create(channel, lineUserOne));
        channelLineUserTwo = channelLineUserRepository.save(
            ChannelLineUser.create(channel, lineUserTwo));

        userBlankLineUser = userRepository.save(User.createUser(new PhoneNumber("010-1234-1234")));
        userWithLineUser = userRepository.save(
            User.createUserWithLine(new PhoneNumber("010-1234-1235"), lineUserOne));

    }

    @Test
    public void 휴대전화_입력시_비어있는_경우_조회_및_변경() {
        // given
        assertThat(userBlankLineUser.getLineUser()).isNull();
        String givenPhoneNumber = "010-1234-1234";

        when(lineService.findLineUserByPhoneNumber(anyString())).thenReturn(
            Optional.empty());

        // when
        boolean b = channelHookService.validatePhoneAndMatchUser(givenPhoneNumber,
            channelLineUserTwo);

        System.out.println("이런 시발 : " + b);

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
            .isEqualTo(lineUserOne.getLineId());

        // when
        boolean b = channelHookService.validatePhoneAndMatchUser("010-1234-1235",
            channelLineUserTwo);
        // then
        assertThat(userWithLineUser.getLineUser().getLineId())
            .isEqualTo(lineUserOne.getLineId());
        assertThat(b).isFalse();
    }

    //TODO: 여기 자꾸 테스트 에러 생김 확인해보자
    @Test
    public void 성공_휴대전화_입력_시_단일전송_로직_테스트() {
        // given
        doNothing().when(messageUtils).registerSingleTask(
            any(Channel.class), any(LineUser.class), any(), any(LocalDateTime.class));
        LineUser mockLineUser = mock(LineUser.class);
        when(mockLineUser.getLineId()).thenReturn("lineId1");
        when(lineService.findLineUserByLineId(anyString())).thenReturn(Optional.of(mockLineUser));

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
        doNothing().when(messageUtils).registerSingleTask(
            any(Channel.class), any(LineUser.class), anyString(), any(LocalDateTime.class));
        LineUser mockLineUser = mock(LineUser.class);
        when(mockLineUser.getLineId()).thenReturn("lineId1");
        when(lineService.findLineUserByLineId(anyString())).thenReturn(Optional.of(mockLineUser));

        String wrongPhoneNumber = "010-12-1234";

        // when
        channelHookService.sendLineMessageAfterSuccess(channel.getId(), lineUserOne.getLineId(),
            wrongPhoneNumber);

        // then
        verify(messageUtils, never()).registerTaskSchedule(any(), any(LocalDateTime.class));
    }

}