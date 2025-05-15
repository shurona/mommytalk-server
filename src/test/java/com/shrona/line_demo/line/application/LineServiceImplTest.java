package com.shrona.line_demo.line.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shrona.line_demo.line.infrastructure.LineMessageJpaRepository;
import com.shrona.line_demo.line.infrastructure.LineUserJpaRepository;
import com.shrona.line_demo.user.domain.vo.PhoneNumber;
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
    private LineUserJpaRepository lineUserRepository;
    @Mock
    private LineMessageJpaRepository lineMessageRepository;

    @DisplayName("휴대전화 입력 정합성 테스트")
    @Test
    public void 휴대전화번호_정합성_테스트() {
        // given
        String correctOne = "010-2202-2020";    // 올바른 하이픈 형식
        String correctTwo = "010 2202 2020";    // 올바른 공백 형식
        String correctThree = "010 202 2020";    // 올바른 공백 형식 - 2
        String wrongOne = "010 2202-2020";      // 하이픈/공백 혼용 (잘못된 형식)
        String wrongTwo = "01-2202-2020";       // 앞자리 숫자 개수 오류 (잘못된 형식)
        String wrongThree = "010-2202-202";     // 마지막 숫자 개수 오류 (잘못된 형식)
        String wrongFour = "일반 text";          // 일반 텍스트 (잘못된 형식)

        // when
        // then
        assertThat(lineService.isValidFormat(correctOne)).isTrue();
        assertThat(lineService.isValidFormat(correctTwo)).isTrue();
        assertThat(lineService.isValidFormat(correctThree)).isTrue();
        assertThat(lineService.isValidFormat(wrongOne)).isFalse();
        assertThat(lineService.isValidFormat(wrongTwo)).isFalse();
        assertThat(lineService.isValidFormat(wrongThree)).isFalse();
        assertThat(lineService.isValidFormat(wrongFour)).isFalse();

    }

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

}