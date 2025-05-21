package com.shrona.line_demo.line.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shrona.line_demo.line.domain.LineUser;
import com.shrona.line_demo.line.infrastructure.LineUserJpaRepository;
import com.shrona.line_demo.user.domain.User;
import com.shrona.line_demo.user.domain.vo.PhoneNumber;
import com.shrona.line_demo.user.infrastructure.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


@Transactional
@SpringBootTest
class LineServiceImplTest {

    @Autowired
    private LineServiceImpl lineService;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private LineUserJpaRepository lineUserRepository;


    private LineUser lineUserOne;
    private LineUser lineUserTwo;
    private User userBlankLineUser;
    private User userWithLineUser;


    @BeforeEach
    public void beforeEach() {
        lineUserOne = lineUserRepository.save(LineUser.createLineUser("lineOne"));
        lineUserTwo = lineUserRepository.save(LineUser.createLineUser("lineTwo"));
        userBlankLineUser = userRepository.save(User.createUser(new PhoneNumber("010-1234-1234")));
        userWithLineUser = userRepository.save(
            User.createUserWithLine(new PhoneNumber("010-1234-1235"), lineUserOne));

    }

    @Test
    public void 비어있는_경우_조회_및_변경() {
        // given
        assertThat(userBlankLineUser.getLineId()).isNull();

        // when
        lineService.validatePhoneAndMatchUser("010-1234-1234", lineUserTwo);
        // then
        assertThat(userBlankLineUser.getLineId()).isEqualTo(lineUserTwo.getLineId());
    }

    @Test
    public void 이미있는_경우_미_변경() {
        // given
        assertThat(userWithLineUser.getLineId()).isEqualTo(lineUserOne.getLineId());
        // when
        lineService.validatePhoneAndMatchUser("010-1234-1235", lineUserTwo);
        // then
        assertThat(userWithLineUser.getLineId()).isEqualTo(lineUserOne.getLineId());
    }
}