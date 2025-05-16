package com.shrona.line_demo.user.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shrona.line_demo.line.application.LineServiceImpl;
import com.shrona.line_demo.line.domain.LineUser;
import com.shrona.line_demo.user.domain.User;
import com.shrona.line_demo.user.domain.vo.PhoneNumber;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class UserServiceImplTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private LineServiceImpl lineService;

    @PersistenceContext
    private EntityManager entityManager;

    private List<User> userList;
    private List<String> phoneNumberList;

    @BeforeEach
    void setUp() {

        String number1 = "010-2345-6789";
        String number2 = "010-3456-7890";
        String number3 = "010-4567-8901";
        String number4 = "010-1234-5678";
        String number5 = "010-5678-9012";
        String number6 = "010-6789-0123";
        String number7 = "010-7890-1234";
        String number8 = "010-8901-2345";
        String number9 = "010-9012-3456";
        String number10 = "010-0123-4567";

        phoneNumberList = new ArrayList<>(List.of
            (number1, number2, number3, number4, number5, number6, number7, number8, number9,
                number10));

        LineUser lineUser1 = lineService.followLineUserByLineId("line1");
        LineUser lineUser2 = lineService.followLineUserByLineId("line2");
        LineUser lineUser3 = lineService.followLineUserByLineId("line3");
        LineUser lineUser4 = lineService.followLineUserByLineId("line4");
        LineUser lineUser5 = lineService.followLineUserByLineId("line5");

        lineUser1.settingPhoneNumber(new PhoneNumber(number6));
        lineUser2.settingPhoneNumber(new PhoneNumber(number7));
        lineUser3.settingPhoneNumber(new PhoneNumber(number8));
        lineUser4.settingPhoneNumber(new PhoneNumber(number9));
        lineUser5.settingPhoneNumber(new PhoneNumber(number10));

        userList = new ArrayList<>(List.of(
            User.createUser(new PhoneNumber(number1)),
            User.createUser(new PhoneNumber(number2)),
            User.createUser(new PhoneNumber(number3)),
            User.createUser(new PhoneNumber(number4)),
            User.createUser(new PhoneNumber(number5)),
            User.createUserWithLine(new PhoneNumber(number6), lineUser1),
            User.createUserWithLine(new PhoneNumber(number7), lineUser2),
            User.createUserWithLine(new PhoneNumber(number8), lineUser3),
            User.createUserWithLine(new PhoneNumber(number9), lineUser4),
            User.createUserWithLine(new PhoneNumber(number10), lineUser5)
        ));
    }


    @DisplayName("기본 설정 테스트")
    @Test
    public void 기본_설정_테스트() {
        assertThat(userList.size()).isEqualTo(10);

        List<LineUser> lineUserList = lineService.findLineUserList(PageRequest.of(0, 100))
            .stream().toList();
        assertThat(lineUserList.size()).isEqualTo(5);

    }

    @DisplayName("사용자 그룹이 기본 기능 테스트")
    @Test
    void 사용자그룹_추가_확인() {

        // given
        String correctPhone = "010-2234-8283";
        String wrongPhone = "03-399-3932";

        phoneNumberList.add(correctPhone);
        phoneNumberList.add(wrongPhone);

        // when
        List<User> userListAfterSave = userService.findOrCreateUsersByPhoneNumbers(
            phoneNumberList);

        // then
        assertThat(userListAfterSave.size()).isEqualTo(11);
    }

    @DisplayName("라인 유저가 존재하면 매칭이 되는지 테스트")
    @Test
    void 라인_유저_매칭_테스트() {

        // given
        String correctPhone = "010-2234-8283";
        String wrongPhone = "03-399-3932";
        String lineId = "newLineId";

        LineUser lineUser = lineService.followLineUserByLineId(lineId);
        lineUser.settingPhoneNumber(new PhoneNumber(correctPhone));

        entityManager.flush();

        phoneNumberList.add(correctPhone);
        phoneNumberList.add(wrongPhone);

        // when
        List<User> userListAfterSave = userService.findOrCreateUsersByPhoneNumbers(
            phoneNumberList);

        User newUser = userService.findUserByPhoneNumber(correctPhone);

        // then
        assertThat(newUser.getLineId()).isEqualTo(lineId);
    }

}