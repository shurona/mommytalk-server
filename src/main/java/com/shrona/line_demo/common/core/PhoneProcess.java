package com.shrona.line_demo.common.core;

import static com.shrona.line_demo.user.domain.vo.PhoneNumber.PHONE_NUMBER_PATTERN;
import static com.shrona.line_demo.user.domain.vo.PhoneNumber.PHONE_NUMBER_PATTERN_TWO;

import com.shrona.line_demo.user.domain.vo.PhoneNumber;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class PhoneProcess {

    /*
        휴대전화 입력 검증
     */
    public boolean isValidFormat(String input) {
        return input.matches(PHONE_NUMBER_PATTERN) || input.matches(PHONE_NUMBER_PATTERN_TWO);
    }


    public List<PhoneNumber> validateAndConvertPhoneNumbers(List<String> phoneList) {
        return phoneList.stream()
            .map(PhoneNumber::changeWithoutError)
            .filter(Objects::nonNull)
            .toList();
    }

}
