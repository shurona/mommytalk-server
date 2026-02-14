package com.shrona.mommytalk.user.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shrona.mommytalk.user.common.exception.UserException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PhoneNumber VO 테스트")
class PhoneNumberTest {

    @Nested
    @DisplayName("미국 전화번호 생성")
    class USPhoneNumberCreation {

        @Test
        @DisplayName("하이픈 포함 미국 번호 - 유효")
        void 하이픈_포함_미국_번호_유효() {
            // given
            String input = "1-650-123-4567";

            // when
            PhoneNumber phoneNumber = new PhoneNumber(input);

            // then
            assertThat(phoneNumber.getPhoneNumber()).isEqualTo("1-650-123-4567");
        }

        @Test
        @DisplayName("공백 포함 미국 번호 - 하이픈으로 변환")
        void 공백_포함_미국_번호_하이픈_변환() {
            // given
            String input = "1 650 123 4567";

            // when
            PhoneNumber phoneNumber = new PhoneNumber(input);

            // then
            assertThat(phoneNumber.getPhoneNumber()).isEqualTo("1-650-123-4567");
        }

        @Test
        @DisplayName("순수 숫자 미국 번호 - 자동 포맷팅")
        void 순수_숫자_미국_번호_자동_포맷팅() {
            // given
            String input = "16501234567";

            // when
            PhoneNumber phoneNumber = new PhoneNumber(input);

            // then
            assertThat(phoneNumber.getPhoneNumber()).isEqualTo("1-650-123-4567");
        }

        @Test
        @DisplayName("다양한 지역번호 - 유효")
        void 다양한_지역번호_유효() {
            // California
            PhoneNumber ca = new PhoneNumber("1-415-555-1234");
            assertThat(ca.getPhoneNumber()).isEqualTo("1-415-555-1234");

            // New York
            PhoneNumber ny = new PhoneNumber("1-212-555-6789");
            assertThat(ny.getPhoneNumber()).isEqualTo("1-212-555-6789");

            // Texas
            PhoneNumber tx = new PhoneNumber("1-713-555-0000");
            assertThat(tx.getPhoneNumber()).isEqualTo("1-713-555-0000");
        }
    }

    @Nested
    @DisplayName("영국 전화번호 생성")
    class UKPhoneNumberCreation {

        @Test
        @DisplayName("하이픈 포함 영국 번호 - 유효")
        void 하이픈_포함_영국_번호_유효() {
            // given
            String input = "44-791-112-3456";

            // when
            PhoneNumber phoneNumber = new PhoneNumber(input);

            // then
            assertThat(phoneNumber.getPhoneNumber()).isEqualTo("44-791-112-3456");
        }

        @Test
        @DisplayName("공백 포함 영국 번호 - 하이픈으로 변환")
        void 공백_포함_영국_번호_하이픈_변환() {
            // given
            String input = "44 791 112 3456";

            // when
            PhoneNumber phoneNumber = new PhoneNumber(input);

            // then
            assertThat(phoneNumber.getPhoneNumber()).isEqualTo("44-791-112-3456");
        }

        @Test
        @DisplayName("순수 숫자 영국 번호 - 자동 포맷팅")
        void 순수_숫자_영국_번호_자동_포맷팅() {
            // given
            String input = "447911123456";

            // when
            PhoneNumber phoneNumber = new PhoneNumber(input);

            // then
            assertThat(phoneNumber.getPhoneNumber()).isEqualTo("44-791-112-3456");
        }

        @Test
        @DisplayName("다양한 영국 번호 - 유효")
        void 다양한_영국_번호_유효() {
            // London
            PhoneNumber london = new PhoneNumber("44-207-946-0958");
            assertThat(london.getPhoneNumber()).isEqualTo("44-207-946-0958");

            // Mobile
            PhoneNumber mobile = new PhoneNumber("44-750-555-1234");
            assertThat(mobile.getPhoneNumber()).isEqualTo("44-750-555-1234");
        }
    }

    @Nested
    @DisplayName("한국 전화번호 기존 동작 유지")
    class KoreanPhoneNumberBackwardCompatibility {

        @Test
        @DisplayName("한국 번호 - 하이픈 포함 11자리")
        void 한국_번호_하이픈_포함_11자리() {
            PhoneNumber phone = new PhoneNumber("010-1234-5678");
            assertThat(phone.getPhoneNumber()).isEqualTo("010-1234-5678");
        }

        @Test
        @DisplayName("한국 번호 - 순수 숫자 11자리 자동 포맷팅")
        void 한국_번호_순수_숫자_11자리_자동_포맷팅() {
            PhoneNumber phone = new PhoneNumber("01012345678");
            assertThat(phone.getPhoneNumber()).isEqualTo("010-1234-5678");
        }

        @Test
        @DisplayName("한국 번호 - 공백 포함시 하이픈으로 변환")
        void 한국_번호_공백_포함시_하이픈_변환() {
            PhoneNumber phone = new PhoneNumber("010 1234 5678");
            assertThat(phone.getPhoneNumber()).isEqualTo("010-1234-5678");
        }

        @Test
        @DisplayName("한국 번호 - 070 번호")
        void 한국_번호_070번호() {
            PhoneNumber phone = new PhoneNumber("07012345678");
            assertThat(phone.getPhoneNumber()).isEqualTo("070-1234-5678");
        }
    }

    @Nested
    @DisplayName("잘못된 형식 검증")
    class InvalidFormatValidation {

        @Test
        @DisplayName("국가 코드 2로 시작 - 미지원")
        void 국가코드_2로_시작_미지원() {
            assertThatThrownBy(() -> new PhoneNumber("2-650-123-4567"))
                .isInstanceOf(UserException.class);
        }

        @Test
        @DisplayName("미국 번호 자릿수 부족 - 10자리")
        void 미국_번호_자릿수_부족() {
            assertThatThrownBy(() -> new PhoneNumber("1-650-123-456"))
                .isInstanceOf(UserException.class);
        }

        @Test
        @DisplayName("미국 번호 자릿수 초과 - 12자리")
        void 미국_번호_자릿수_초과() {
            assertThatThrownBy(() -> new PhoneNumber("1-650-123-45678"))
                .isInstanceOf(UserException.class);
        }

        @Test
        @DisplayName("순수 숫자 10자리 - 국가코드 없음")
        void 순수_숫자_10자리_국가코드_없음() {
            // 6501234567은 국가 코드가 없으므로 유효하지 않음
            assertThatThrownBy(() -> new PhoneNumber("6501234567"))
                .isInstanceOf(UserException.class);
        }

        @Test
        @DisplayName("하이픈과 공백 혼용 - 공백이 하이픈으로 정규화됨")
        void 하이픈과_공백_혼용_공백_정규화() {
            // given
            String input = "1-650 123-4567";

            // when
            PhoneNumber phoneNumber = new PhoneNumber(input);

            // then
            // 공백이 하이픈으로 정규화되어 유효한 번호로 저장됨 (어드민/카카오 입력용)
            assertThat(phoneNumber.getPhoneNumber()).isEqualTo("1-650-123-4567");
        }

        @Test
        @DisplayName("빈 문자열 - 유효하지 않음")
        void 빈_문자열_유효하지않음() {
            assertThatThrownBy(() -> new PhoneNumber(""))
                .isInstanceOf(UserException.class);

            assertThatThrownBy(() -> new PhoneNumber("   "))
                .isInstanceOf(UserException.class);
        }

        @Test
        @DisplayName("null - 유효하지 않음")
        void null_유효하지않음() {
            assertThatThrownBy(() -> new PhoneNumber(null))
                .isInstanceOf(UserException.class);
        }
    }

    @Nested
    @DisplayName("국가 구분 테스트")
    class CountryDistinction {

        @Test
        @DisplayName("첫 자리 0 - 한국 번호로 인식")
        void 첫자리_0_한국번호() {
            PhoneNumber phone = new PhoneNumber("01012345678");
            assertThat(phone.getPhoneNumber()).startsWith("010");
        }

        @Test
        @DisplayName("첫 자리 1 - 미국 번호로 인식")
        void 첫자리_1_미국번호() {
            PhoneNumber phone = new PhoneNumber("16501234567");
            assertThat(phone.getPhoneNumber()).startsWith("1-");
        }

        @Test
        @DisplayName("한국과 미국 번호 동시 사용 가능")
        void 한국과_미국_번호_동시사용() {
            PhoneNumber korean = new PhoneNumber("010-1234-5678");
            PhoneNumber us = new PhoneNumber("1-650-123-4567");

            assertThat(korean.getPhoneNumber()).isEqualTo("010-1234-5678");
            assertThat(us.getPhoneNumber()).isEqualTo("1-650-123-4567");
            assertThat(korean).isNotEqualTo(us);
        }
    }

    @Nested
    @DisplayName("changeWithoutError 메서드 테스트")
    class ChangeWithoutErrorTests {

        @Test
        @DisplayName("유효한 미국 번호 - 성공")
        void 유효한_미국번호_성공() {
            PhoneNumber result = PhoneNumber.changeWithoutError("1-650-123-4567");
            assertThat(result).isNotNull();
            assertThat(result.getPhoneNumber()).isEqualTo("1-650-123-4567");
        }

        @Test
        @DisplayName("유효한 한국 번호 - 성공")
        void 유효한_한국번호_성공() {
            PhoneNumber result = PhoneNumber.changeWithoutError("010-1234-5678");
            assertThat(result).isNotNull();
            assertThat(result.getPhoneNumber()).isEqualTo("010-1234-5678");
        }

        @Test
        @DisplayName("유효하지 않은 번호 - null 반환")
        void 유효하지않은_번호_null반환() {
            PhoneNumber result = PhoneNumber.changeWithoutError("invalid");
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("빈 문자열 - null 반환")
        void 빈문자열_null반환() {
            PhoneNumber result = PhoneNumber.changeWithoutError("");
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Equals와 HashCode 테스트")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("동일한 미국 번호 - equals true")
        void 동일한_미국번호_equals_true() {
            PhoneNumber phone1 = new PhoneNumber("1-650-123-4567");
            PhoneNumber phone2 = new PhoneNumber("1-650-123-4567");

            assertThat(phone1).isEqualTo(phone2);
            assertThat(phone1.hashCode()).isEqualTo(phone2.hashCode());
        }

        @Test
        @DisplayName("다른 번호 - equals false")
        void 다른_번호_equals_false() {
            PhoneNumber phone1 = new PhoneNumber("1-650-123-4567");
            PhoneNumber phone2 = new PhoneNumber("1-415-555-1234");

            assertThat(phone1).isNotEqualTo(phone2);
        }

        @Test
        @DisplayName("순수 숫자와 하이픈 포함 - equals true (동일 번호)")
        void 순수숫자와_하이픈포함_equals_true() {
            PhoneNumber phone1 = new PhoneNumber("16501234567");
            PhoneNumber phone2 = new PhoneNumber("1-650-123-4567");

            // 둘 다 1-650-123-4567로 저장되므로 동일
            assertThat(phone1).isEqualTo(phone2);
        }

        @Test
        @DisplayName("한국 번호 - 순수 숫자와 하이픈 포함 equals true")
        void 한국번호_순수숫자와_하이픈포함_equals_true() {
            PhoneNumber phone1 = new PhoneNumber("01012345678");
            PhoneNumber phone2 = new PhoneNumber("010-1234-5678");

            assertThat(phone1).isEqualTo(phone2);
        }
    }

    @Nested
    @DisplayName("checkValidPhoneNumber 정적 메서드 테스트")
    class CheckValidPhoneNumberTests {

        @Test
        @DisplayName("유효한 미국 번호 - false 반환")
        void 유효한_미국번호_false반환() {
            // checkValidPhoneNumber는 유효하지 않으면 true 반환
            assertThat(PhoneNumber.checkValidPhoneNumber("1-650-123-4567")).isFalse();
            assertThat(PhoneNumber.checkValidPhoneNumber("16501234567")).isFalse();
        }

        @Test
        @DisplayName("유효한 한국 번호 - false 반환")
        void 유효한_한국번호_false반환() {
            assertThat(PhoneNumber.checkValidPhoneNumber("010-1234-5678")).isFalse();
            assertThat(PhoneNumber.checkValidPhoneNumber("01012345678")).isFalse();
        }

        @Test
        @DisplayName("유효하지 않은 번호 - true 반환")
        void 유효하지않은_번호_true반환() {
            assertThat(PhoneNumber.checkValidPhoneNumber("invalid")).isTrue();
            assertThat(PhoneNumber.checkValidPhoneNumber("123")).isTrue();
        }

        @Test
        @DisplayName("빈 문자열 - true 반환")
        void 빈문자열_true반환() {
            assertThat(PhoneNumber.checkValidPhoneNumber("")).isTrue();
            assertThat(PhoneNumber.checkValidPhoneNumber("   ")).isTrue();
        }

        @Test
        @DisplayName("null - true 반환")
        void null_true반환() {
            assertThat(PhoneNumber.checkValidPhoneNumber(null)).isTrue();
        }
    }
}
