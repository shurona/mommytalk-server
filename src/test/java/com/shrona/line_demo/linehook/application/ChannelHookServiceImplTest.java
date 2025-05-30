package com.shrona.line_demo.linehook.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shrona.line_demo.line.domain.Channel;
import com.shrona.line_demo.line.domain.ChannelLineUser;
import com.shrona.line_demo.line.domain.LineUser;
import com.shrona.line_demo.line.infrastructure.ChannelJpaRepository;
import com.shrona.line_demo.line.infrastructure.ChannelLineUserJpaRepository;
import com.shrona.line_demo.line.infrastructure.LineUserJpaRepository;
import com.shrona.line_demo.user.domain.User;
import com.shrona.line_demo.user.domain.vo.PhoneNumber;
import com.shrona.line_demo.user.infrastructure.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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


    private LineUser lineUserOne;
    private LineUser lineUserTwo;
    private ChannelLineUser channelLineUserOne;
    private ChannelLineUser channelLineUserTwo;
    private User userBlankLineUser;
    private User userWithLineUser;


    @BeforeEach
    public void beforeEach() {
        Channel channel = Channel.createChannel("name", "");
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
        assertThat(userBlankLineUser.getLineId()).isNull();

        // when
        channelHookService.validatePhoneAndMatchUser("010-1234-1234", channelLineUserTwo);
        // then
        assertThat(userBlankLineUser.getLineId()).isEqualTo(lineUserTwo.getLineId());
    }

    @Test
    public void 휴대전화_입력시_이미있는_경우_미_변경() {
        // given
        assertThat(userWithLineUser.getLineId()).isEqualTo(lineUserOne.getLineId());
        // when
        channelHookService.validatePhoneAndMatchUser("010-1234-1235", channelLineUserTwo);
        // then
        assertThat(userWithLineUser.getLineId()).isEqualTo(lineUserOne.getLineId());
    }

}