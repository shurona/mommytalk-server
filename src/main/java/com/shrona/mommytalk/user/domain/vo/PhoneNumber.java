package com.shrona.mommytalk.user.domain.vo;

import static com.shrona.mommytalk.user.common.exception.UserErrorCode.INVALID_PHONE_NUMBER_INPUT;

import com.shrona.mommytalk.user.common.exception.UserException;
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

    public final static String PHONE_NUMBER_PATTERN = "\\d{3}([- ])\\d{3,4}\\1\\d{4}";
    public final static String PHONE_NUMBER_PATTERN_TWO = "(0\\d0\\d{7,8})";

    /**
     * 미국 전화번호 패턴 (하이픈 또는 공백 포함)
     * 형식: 1-650-123-4567 또는 1 650 123 4567
     * 국가코드(1) + 지역번호(3자리) + 앞번호(3자리) + 뒷번호(4자리)
     */
    public final static String US_PHONE_NUMBER_PATTERN = "1([- ])\\d{3}\\1\\d{3}\\1\\d{4}";

    /**
     * 미국 전화번호 패턴 (순수 숫자)
     * 형식: 16501234567 (11자리, 1로 시작)
     */
    public final static String US_PHONE_NUMBER_PATTERN_PURE = "(1\\d{10})";

    /**
     * 영국 전화번호 패턴 (하이픈 또는 공백 포함)
     * 형식: 44-791-112-3456 또는 44 791 112 3456
     * 국가코드(44) + 10자리를 3-3-4로 분리
     */
    public final static String UK_PHONE_NUMBER_PATTERN = "44([- ])\\d{3}\\1\\d{3}\\1\\d{4}";

    /**
     * 영국 전화번호 패턴 (순수 숫자)
     * 형식: 447911123456 (12자리, 44로 시작)
     */
    public final static String UK_PHONE_NUMBER_PATTERN_PURE = "(44\\d{10})";

    @Column(name = "phone_number", unique = true, nullable = true)
    private String phoneNumber;

    public PhoneNumber(String phoneNumber) {
        // Check Valid PhoneNumber
        if (checkValidPhoneNumber(phoneNumber)) {
            throw new UserException(INVALID_PHONE_NUMBER_INPUT);
        }

        // 공백을 하이픈으로 통일
        phoneNumber = phoneNumber.replace(' ', '-');

        // Pattern 컴파일
        Pattern koreanHyphenPattern = Pattern.compile(PHONE_NUMBER_PATTERN);
        Pattern koreanPurePattern = Pattern.compile(PHONE_NUMBER_PATTERN_TWO);
        Pattern usHyphenPattern = Pattern.compile(US_PHONE_NUMBER_PATTERN);
        Pattern usPurePattern = Pattern.compile(US_PHONE_NUMBER_PATTERN_PURE);
        Pattern ukHyphenPattern = Pattern.compile(UK_PHONE_NUMBER_PATTERN);
        Pattern ukPurePattern = Pattern.compile(UK_PHONE_NUMBER_PATTERN_PURE);

        // 이미 하이픈 포함된 경우 (한국, 미국 또는 영국)
        if (koreanHyphenPattern.matcher(phoneNumber).matches() ||
            usHyphenPattern.matcher(phoneNumber).matches() ||
            ukHyphenPattern.matcher(phoneNumber).matches()) {
            this.phoneNumber = phoneNumber;
            return;
        }

        // 순수 숫자인 경우 - 첫 자리로 국가 판단
        if (phoneNumber.matches("\\d+")) {
            char firstDigit = phoneNumber.charAt(0);

            // 미국 번호: 1로 시작하는 11자리
            if (firstDigit == '1' && phoneNumber.length() == 11) {
                if (usPurePattern.matcher(phoneNumber).matches()) {
                    // 16501234567 → 1-650-123-4567
                    this.phoneNumber = phoneNumber.substring(0, 1) + "-" +
                                       phoneNumber.substring(1, 4) + "-" +
                                       phoneNumber.substring(4, 7) + "-" +
                                       phoneNumber.substring(7);
                    return;
                }
            }

            // 영국 번호: 44로 시작하는 12자리
            if (firstDigit == '4' && phoneNumber.length() == 12) {
                if (ukPurePattern.matcher(phoneNumber).matches()) {
                    // 447911123456 → 44-791-112-3456
                    this.phoneNumber = phoneNumber.substring(0, 2) + "-" +
                                       phoneNumber.substring(2, 5) + "-" +
                                       phoneNumber.substring(5, 8) + "-" +
                                       phoneNumber.substring(8);
                    return;
                }
            }

            // 한국 번호: 0으로 시작 (기존 로직 유지)
            if (firstDigit == '0' && koreanPurePattern.matcher(phoneNumber).matches()) {
                // 11자리 한국 번호
                if (phoneNumber.length() == 11) {
                    this.phoneNumber = phoneNumber.substring(0, 3) + "-" +
                                       phoneNumber.substring(3, 7) + "-" +
                                       phoneNumber.substring(7);
                    return;
                }
                // 10자리 한국 번호
                if (phoneNumber.length() == 10) {
                    this.phoneNumber = phoneNumber.substring(0, 3) + "-" +
                                       phoneNumber.substring(3, 6) + "-" +
                                       phoneNumber.substring(6);
                    return;
                }
            }
        }

        // Fallback: 원본 저장 (validation은 이미 통과함)
        this.phoneNumber = phoneNumber;
    }

    public static PhoneNumber changeWithoutError(String phoneNumber) {
        // Check Valid PhoneNumber
        if (checkValidPhoneNumber(phoneNumber)) {
            return null;
        }

        return new PhoneNumber(phoneNumber);
    }

    /**
     * 전화번호 유효성 검증 (한국 + 미국)
     * @param inputPhone 검증할 전화번호
     * @return true: 유효하지 않음, false: 유효함
     */
    public static boolean checkValidPhoneNumber(String inputPhone) {
        if (!StringUtils.hasText(inputPhone)) {
            return true; // 빈 문자열은 유효하지 않음
        }

        // 공백을 하이픈으로 정규화
        String normalized = inputPhone.replace(' ', '-');

        // 한국 번호 패턴
        boolean isKorean = Pattern.matches(PHONE_NUMBER_PATTERN, normalized) ||
                           Pattern.matches(PHONE_NUMBER_PATTERN_TWO, normalized);

        // 미국 번호 패턴
        boolean isUS = Pattern.matches(US_PHONE_NUMBER_PATTERN, normalized) ||
                       Pattern.matches(US_PHONE_NUMBER_PATTERN_PURE, normalized);

        // 영국 번호 패턴
        boolean isUK = Pattern.matches(UK_PHONE_NUMBER_PATTERN, normalized) ||
                       Pattern.matches(UK_PHONE_NUMBER_PATTERN_PURE, normalized);

        // 모두 아니면 유효하지 않음
        return !(isKorean || isUS || isUK);
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