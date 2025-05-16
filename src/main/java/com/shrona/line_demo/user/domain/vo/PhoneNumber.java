package com.shrona.line_demo.user.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class PhoneNumber {

    public final static String PHONE_NUMBER_PATTERN = "^\\d{3}-\\d{3,4}-\\d{4}$";
    @Column(name = "phone_number", unique = true, nullable = true)
    private String phoneNumber;

    public PhoneNumber(String phoneNumber) {
        // 공백의 경우 -로 통일
        if (phoneNumber.charAt(3) == ' ') {
            phoneNumber = phoneNumber.replace(' ', '-');
        }

        // Check Valid PhoneNumber
        if (!StringUtils.hasText(phoneNumber) || !Pattern.matches(PHONE_NUMBER_PATTERN,
            phoneNumber)) {
            throw new IllegalArgumentException();
//            throw new UserException(ExceptionMessage.INVALID_PHONE_NUMBER_INPUT);
        }

        this.phoneNumber = phoneNumber;
    }

    public static PhoneNumber changeWithoutError(String phoneNumber) {
        // Check Valid PhoneNumber
        if (!StringUtils.hasText(phoneNumber) || !Pattern.matches(PHONE_NUMBER_PATTERN,
            phoneNumber)) {
            return null;
        }

        return new PhoneNumber(phoneNumber);
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