package com.shrona.mommytalk.common.utils;

import static com.shrona.mommytalk.user.domain.vo.PhoneNumber.PHONE_NUMBER_PATTERN;
import static com.shrona.mommytalk.user.domain.vo.PhoneNumber.PHONE_NUMBER_PATTERN_TWO;

import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
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
