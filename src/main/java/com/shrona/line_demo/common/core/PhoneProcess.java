package com.shrona.line_demo.common.core;

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
        String regex = "(\\d{3}([- ])\\d{3,4}\\2\\d{4})|(0\\d0\\d{7,8})";
        return input.matches(regex);
    }


    public List<PhoneNumber> validateAndConvertPhoneNumbers(List<String> phoneList) {
        return phoneList.stream()
            .map(PhoneNumber::changeWithoutError)
            .filter(Objects::nonNull)
            .toList();
    }

}
