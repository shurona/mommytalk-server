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
    private LineUser SavedLineUserTwo;


    private String beforePhoneNumber = "010-1234-5678";
    private String afterPhoneNumber = "010-8765-4321";

    @BeforeEach
    void beforeEach() {
        // 테스트용 LineUser와 User 저장
        savedLineUser = lineUserRepository.save(LineUser.createLineUser("lineId"));
        SavedLineUserTwo = lineUserRepository.save(LineUser.createLineUser("lineId-2"));

        // 테스트용 유저 저장
        userRepository.save(User.createUserWithLine(
            PhoneNumber.changeWithoutError(beforePhoneNumber), savedLineUser));
        userRepository.save(
            User.createUser(PhoneNumber.changeWithoutError(afterPhoneNumber)));


    }

    @Test
    @DisplayName("라인유저의 휴대전화 번호 변경 정상 로직")
    void updateLineUserPhoneNumber_success() {
        // given
        String newPhoneNumber = "010-8765-4991";

        // when
        LineUser updatedLineUser = lineUserService.updateLineUserPhoneNumber(savedLineUser.getId(),
            newPhoneNumber);

        // then
        User updatedUser = userRepository.findByPhoneNumber(
            PhoneNumber.changeWithoutError(newPhoneNumber)).orElse(null);

        User beforeUser = userRepository.findByPhoneNumber(
            PhoneNumber.changeWithoutError(beforePhoneNumber)).orElse(null);

        assertThat(beforeUser).isNull();
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getLineUser().getLineId()).isEqualTo(updatedLineUser.getLineId());
    }

    @Test
    @DisplayName("이미 존재하는 번호 - 라인 유저가 이미 있는 경우")
    void 번호가_존재하지만_라인_유저가_있는_경우() {
        // given
        String duplicatePhone = beforePhoneNumber;

        // when & then
        assertThatThrownBy(() ->
            lineUserService.updateLineUserPhoneNumber(savedLineUser.getId(), duplicatePhone)
        ).isInstanceOf(LineException.class);
    }

}