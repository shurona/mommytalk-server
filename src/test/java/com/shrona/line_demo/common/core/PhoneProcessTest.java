package com.shrona.line_demo.common.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.shrona.line_demo.user.domain.vo.PhoneNumber;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PhoneProcessTest {

    @InjectMocks
    PhoneProcess phoneProcess;

    @Test
    void 유효하지_않은_전화번호_필터링() {
        // given
        List<String> inputs = Arrays.asList(
            "010-1234-5678", "070 6789 2222",
            "INVALID", "02-987-6543", "010-1222 9872", "021231234", "10012341234",
            "01012341234", "0101231234");

        // when
        List<PhoneNumber> userList = phoneProcess.validateAndConvertPhoneNumbers(inputs);

        System.out.println(userList);

        // then
        assertThat(userList.size()).isEqualTo(4);
    }

    @DisplayName("휴대전화 입력 정합성 테스트")
    @Test
    public void 휴대전화번호_정합성_테스트() {
        // given
        String correctOne = "010-2202-2020";    // 올바른 하이픈 형식
        String correctTwo = "010 2202 2020";    // 올바른 공백 형식
        String correctThree = "010 202 2020";    // 올바른 공백 형식 - 2
        String correctFour = "01012341234"; // 올바른 연결 형식
        String correctFive = "0101231234"; // 올바른 연결 형식
        String wrongOne = "010 2202-2020";      // 하이픈/공백 혼용 (잘못된 형식)
        String wrongTwo = "01-2202-2020";       // 앞자리 숫자 개수 오류 (잘못된 형식)
        String wrongThree = "010-2202-202";     // 마지막 숫자 개수 오류 (잘못된 형식)
        String wrongFour = "일반 text";          // 일반 텍스트 (잘못된 형식)
        String wrongFive = "021231234"; //연결인데 앞에가 0x0이 아닌 경우

        // when
        // then
        assertThat(phoneProcess.isValidFormat(correctOne)).isTrue();
        assertThat(phoneProcess.isValidFormat(correctTwo)).isTrue();
        assertThat(phoneProcess.isValidFormat(correctThree)).isTrue();
        assertThat(phoneProcess.isValidFormat(correctFour)).isTrue();
        assertThat(phoneProcess.isValidFormat(correctFive)).isTrue();
        assertThat(phoneProcess.isValidFormat(wrongOne)).isFalse();
        assertThat(phoneProcess.isValidFormat(wrongTwo)).isFalse();
        assertThat(phoneProcess.isValidFormat(wrongThree)).isFalse();
        assertThat(phoneProcess.isValidFormat(wrongFour)).isFalse();
        assertThat(phoneProcess.isValidFormat(wrongFive)).isFalse();

    }

}