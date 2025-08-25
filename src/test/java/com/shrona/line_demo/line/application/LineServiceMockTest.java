package com.shrona.line_demo.line.application;

import static com.shrona.line_demo.line.common.exception.LineErrorCode.DUPLICATE_PHONE_NUMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shrona.line_demo.line.common.exception.LineException;
import com.shrona.line_demo.line.domain.LineUser;
import com.shrona.line_demo.line.infrastructure.LineUserJpaRepository;
import com.shrona.line_demo.linehook.infrastructure.LineMessageJpaRepository;
import com.shrona.line_demo.user.application.GroupService;
import com.shrona.line_demo.user.application.UserService;
import com.shrona.line_demo.user.domain.User;
import com.shrona.line_demo.user.domain.vo.PhoneNumber;
import com.shrona.line_demo.user.infrastructure.UserJpaRepository;
import java.util.Optional;
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
    @Mock
    private GroupService groupService;

    @Mock
    private UserService userService;


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

    @DisplayName("라인유저 휴대전화 번호 정상 변경 테스트")
    @Test
    public void 라인유저_휴대전화_변경_테스트() {
        // given
        Long lineUserId = 1L;
        String newPhoneNumber = "010-2323-2323";

        LineUser testLineUser = LineUser.createLineUser("testLineId");
        User existingUser = spy(User.createUser(PhoneNumber.changeWithoutError("010-1111-1111")));

        // Mock 설정
        when(lineUserRepository.findById(lineUserId))
            .thenReturn(Optional.of(testLineUser));
        when(userService.findUserByPhoneNumber(newPhoneNumber))
            .thenReturn(null); // 중복 없음
        when(userService.findUserByLineUser(testLineUser))
            .thenReturn(Optional.of(existingUser));

        // when
        LineUser result = lineService.updateLineUserPhoneNumber(lineUserId, newPhoneNumber);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(testLineUser);

        // 중요: User의 updatePhoneNumber가 호출되었는지 확인
        verify(existingUser).updatePhoneNumber(any(PhoneNumber.class));

    }

    @DisplayName("중복된 전화번호로 변경 시도 시 예외 테스트")
    @Test
    public void 중복_전화번호_변경_예외_테스트() {
        // given
        Long lineUserId = 1L;
        String duplicatePhoneNumber = "010-2323-2323";

        LineUser testLineUser = LineUser.createLineUser("testLineId");
        User existingUserWithSamePhone = User.createUser(
            PhoneNumber.changeWithoutError(duplicatePhoneNumber));

        when(lineUserRepository.findById(lineUserId))
            .thenReturn(Optional.of(testLineUser));
        when(userService.findUserByPhoneNumber(duplicatePhoneNumber))
            .thenReturn(existingUserWithSamePhone); // 중복 전화번호 존재

        // when & then
        assertThatThrownBy(() ->
            lineService.updateLineUserPhoneNumber(lineUserId, duplicatePhoneNumber))
            .isInstanceOf(LineException.class)
            .hasMessageContaining(DUPLICATE_PHONE_NUMBER.getMessage());
    }

    @DisplayName("잘못된 전화번호 형식 예외 테스트")
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