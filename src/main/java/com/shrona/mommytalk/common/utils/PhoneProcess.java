package com.shrona.mommytalk.common.utils;

import static com.shrona.mommytalk.user.domain.vo.PhoneNumber.PHONE_NUMBER_PATTERN;
import static com.shrona.mommytalk.user.domain.vo.PhoneNumber.PHONE_NUMBER_PATTERN_TWO;
import static com.shrona.mommytalk.user.domain.vo.PhoneNumber.US_PHONE_NUMBER_PATTERN;
import static com.shrona.mommytalk.user.domain.vo.PhoneNumber.US_PHONE_NUMBER_PATTERN_PURE;

import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class PhoneProcess {

    /**
     * 휴대전화 입력 형식 검증 (한국 + 미국)
     *
     * @param input 검증할 전화번호 문자열
     * @return true: 유효한 형식, false: 유효하지 않은 형식
     */
    public boolean isValidFormat(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }

        // 공백을 하이픈으로 정규화 / 여기서는 정규화 하지 않음
//        String normalized = input.replace(' ', '-');

        // 한국 번호 패턴
        boolean isKoreanHyphen = input.matches(PHONE_NUMBER_PATTERN);
        boolean isKoreanPure = input.matches(PHONE_NUMBER_PATTERN_TWO);

        // 미국 번호 패턴
        boolean isUSHyphen = input.matches(US_PHONE_NUMBER_PATTERN);
        boolean isUSPure = input.matches(US_PHONE_NUMBER_PATTERN_PURE);

        return isKoreanHyphen || isKoreanPure || isUSHyphen || isUSPure;
    }


    public List<PhoneNumber> validateAndConvertPhoneNumbers(List<String> phoneList) {
        return phoneList.stream()
            .map(PhoneNumber::changeWithoutError)
            .filter(Objects::nonNull)
            .toList();
    }

}
