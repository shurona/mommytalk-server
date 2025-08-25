package com.shrona.mommytalk.user.domain.vo;

import static com.shrona.mommytalk.user.common.exception.UserErrorCode.INVALID_PHONE_NUMBER_INPUT;

import com.shrona.mommytalk.user.common.exception.UserException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class PhoneNumber {

    public final static String PHONE_NUMBER_PATTERN = "\\d{3}([- ])\\d{3,4}\\1\\d{4}";
    public final static String PHONE_NUMBER_PATTERN_TWO = "(0\\d0\\d{7,8})";

    @Column(name = "phone_number", unique = true, nullable = true)
    private String phoneNumber;

    public PhoneNumber(String phoneNumber) {
        // Check Valid PhoneNumber
        if (checkValidPhoneNumber(phoneNumber)) {
            throw new UserException(INVALID_PHONE_NUMBER_INPUT);
        }

        phoneNumber = phoneNumber.replace(' ', '-');

        // 공백의 경우 -로 통일
        Pattern hyphenOrSpacePattern = Pattern.compile(PHONE_NUMBER_PATTERN);
        Pattern pureNumberPattern = Pattern.compile(PHONE_NUMBER_PATTERN_TWO);

        Matcher matcher = hyphenOrSpacePattern.matcher(phoneNumber);
        if (matcher.matches()) {
            // 이미 구분자(하이픈) 있는 번호는 그대로 (구분자가 이미 하이픈으로 정리되어 있음)
            phoneNumber = phoneNumber;
        }

        matcher = pureNumberPattern.matcher(phoneNumber);
        if (matcher.matches()) {
            // 11자리 연속된 숫자일 경우
            if (phoneNumber.length() == 11) {
                phoneNumber = phoneNumber.substring(0, 3) + "-" + phoneNumber.substring(3, 7) + "-"
                    + phoneNumber.substring(7);
            }
            // 10자리 번호 (구 번호, 예: 02-123-4567 같은)
            if (phoneNumber.length() == 10) {
                phoneNumber = phoneNumber.substring(0, 3) + "-" + phoneNumber.substring(3, 6) + "-"
                    + phoneNumber.substring(6);
            }
        }

        this.phoneNumber = phoneNumber;
    }

    public static PhoneNumber changeWithoutError(String phoneNumber) {
        // Check Valid PhoneNumber
        if (checkValidPhoneNumber(phoneNumber)) {
            return null;
        }

        return new PhoneNumber(phoneNumber);
    }

    public static boolean checkValidPhoneNumber(String inputPhone) {
        return !StringUtils.hasText(inputPhone) || (!Pattern.matches(PHONE_NUMBER_PATTERN,
            inputPhone) && !Pattern.matches(PHONE_NUMBER_PATTERN_TWO, inputPhone));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PhoneNumber that = (PhoneNumber) o;
        return Objects.equals(getPhoneNumber(), that.getPhoneNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPhoneNumber());
    }

    @Override
    public String toString() {
        return "PhoneNumber{" +
            "phoneNumber='" + phoneNumber + '\'' +
            '}';
    }

}