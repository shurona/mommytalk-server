package com.shrona.mommytalk.user.infrastructure;

import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.line.infrastructure.LineUserJpaRepository;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class UserJpaRepositoryTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private LineUserJpaRepository lineUserJpaRepository;

    @Test
    public void 번호가_있지만_라인이_빈_경우_조회() {

        // given
        PhoneNumber number = new PhoneNumber("010-3393-1234");
        LineUser lineUser = lineUserJpaRepository.save(LineUser.createLineUser("lineOne"));

        User user = userJpaRepository.save(User.createUser(number));

        // when
        Optional<User> userInfo = userJpaRepository.findByPhoneNumberAndLineUserIsNull(
            number);

        // then
        Assertions.assertThat(userInfo.isPresent()).isTrue();
    }

    @Test
    public void 번호가_있지만_라인이_이미저장되어_있는_경우() {

        // given
        PhoneNumber number = new PhoneNumber("010-3393-1234");
        LineUser lineUser = lineUserJpaRepository.save(LineUser.createLineUser("lineOne"));

        User user = userJpaRepository.save(User.createUserWithLine(number, lineUser));

        // when
        Optional<User> userInfo = userJpaRepository.findByPhoneNumberAndLineUserIsNull(
            number);

        // then
        Assertions.assertThat(userInfo.isPresent()).isFalse();
    }

}