package com.shrona.line_demo.line.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.shrona.line_demo.line.common.exception.LineException;
import com.shrona.line_demo.line.domain.LineUser;
import com.shrona.line_demo.line.infrastructure.LineUserJpaRepository;
import com.shrona.line_demo.linehook.infrastructure.LineMessageJpaRepository;
import com.shrona.line_demo.user.domain.vo.PhoneNumber;
import com.shrona.line_demo.user.infrastructure.UserJpaRepository;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LineServiceMockTest {

    @InjectMocks
    private LineServiceImpl lineService;

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private LineUserJpaRepository lineUserRepository;
    @Mock
    private LineMessageJpaRepository lineMessageRepository;


    @DisplayName("휴대전화 입력 변환 테스트")
    @Test
    public void 휴대전화번호_변환_테스트() {
        // given
        String one = "010-2929-2323";
        String two = "010 3939 3939";
        String changeTwo = "010-3939-3939";

        // when
        PhoneNumber phoneOne = new PhoneNumber(one);
        PhoneNumber phoneTwo = new PhoneNumber(two);

        // then
        assertThat(phoneOne.getPhoneNumber()).isEqualTo(one);
        assertThat(phoneTwo.getPhoneNumber()).isEqualTo(changeTwo);

    }

    @DisplayName("라인유저 휴대전화 번호 변경 테스트")
    @Test
    public void 라인유저_휴대전화_변경_테스트() {

        PhoneNumber mockPhoneNumber = mock(PhoneNumber.class);
        String phoneNumber = "010-2323-2323";
        when(lineUserRepository.findById(anyLong())).thenReturn(Optional.of(
            LineUser.createLineUser("lineIdId")));
        when(userJpaRepository.findByPhoneNumber(any(PhoneNumber.class))).thenReturn(
            Optional.empty());
        when(userJpaRepository.findByPhoneNumber(eq(null))).thenReturn(
            Optional.empty());

        LineUser lineUser = lineService.updateLineUserPhoneNumber(1L, phoneNumber);

        Assertions.assertThat(lineUser.getPhoneNumber().getPhoneNumber()).isEqualTo(phoneNumber);
    }

    @DisplayName("라인유저 휴대전화 번호 잘못 변경 테스트")
    @Test
    public void 라인유저_휴대전화_잘못변경_테스트() {

        String phoneNumber = "010-2323-223";
        when(lineUserRepository.findById(anyLong())).thenReturn(Optional.of(
            LineUser.createLineUser("lineIdId")));

        assertThatThrownBy(() ->
            lineService.updateLineUserPhoneNumber(1L, phoneNumber)
        ).isInstanceOf(LineException.class);
    }

}