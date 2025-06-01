package com.shrona.line_demo.linehook.application;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.shrona.line_demo.line.application.LineService;
import com.shrona.line_demo.line.common.exception.LineException;
import com.shrona.line_demo.line.domain.LineUser;
import com.shrona.line_demo.line.infrastructure.LineUserJpaRepository;
import com.shrona.line_demo.user.domain.User;
import com.shrona.line_demo.user.domain.vo.PhoneNumber;
import com.shrona.line_demo.user.infrastructure.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


@Transactional
@SpringBootTest
class LineServiceImplTest {


    @Autowired
    private LineService lineUserService;
    @Autowired
    private LineUserJpaRepository lineUserRepository;
    @Autowired
    private UserJpaRepository userRepository;
    private LineUser savedLineUser;
    private User afterUser;

    private String beforePhoneNumber = "010-1234-5678";
    private String newPhoneNumber = "010-8765-4321";

    @BeforeEach
    void beforeEach() {
        // 테스트용 LineUser와 User 저장
        PhoneNumber phoneNumber = PhoneNumber.changeWithoutError(beforePhoneNumber);
        savedLineUser = lineUserRepository.save(LineUser.createLineUser("lineId"));
        savedLineUser.settingPhoneNumber(phoneNumber);
        userRepository.save(User.createUser(phoneNumber));
        afterUser = userRepository.save(
            User.createUser(PhoneNumber.changeWithoutError(newPhoneNumber)));


    }

    @Test
    @DisplayName("라인유저의 휴대폰 번호 변경 시 User에도 반영된다")
    void updateLineUserPhoneNumber_success() {
        // given

        // when
        LineUser updatedLineUser = lineUserService.updateLineUserPhoneNumber(savedLineUser.getId(),
            newPhoneNumber);

        // then
        // 변환 확인
        assertThat(updatedLineUser.getPhoneNumber().getPhoneNumber()).isEqualTo(newPhoneNumber);
        assertThat(afterUser.getLineUser().getId()).isEqualTo(savedLineUser.getId());

        // when
        User updatedUser = userRepository.findByPhoneNumber(
            PhoneNumber.changeWithoutError(newPhoneNumber)).orElse(null);

        User beforeUser = userRepository.findByPhoneNumber(
            PhoneNumber.changeWithoutError(beforePhoneNumber)).orElse(null);

        // then
        assertThat(beforeUser).isNull();
        assertThat(updatedUser).isNotNull();
    }

    @Test
    @DisplayName("이미 존재하는 번호로 변경 시 예외 발생")
    void updateLineUserPhoneNumber_duplicatePhone() {
        // given
        String duplicatePhone = beforePhoneNumber;

        // when & then
        assertThatThrownBy(() ->
            lineUserService.updateLineUserPhoneNumber(savedLineUser.getId(), duplicatePhone)
        ).isInstanceOf(LineException.class);
    }


}