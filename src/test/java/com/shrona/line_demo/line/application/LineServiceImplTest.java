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