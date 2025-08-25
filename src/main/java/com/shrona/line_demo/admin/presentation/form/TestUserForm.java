package com.shrona.line_demo.admin.presentation.form;

import com.shrona.line_demo.admin.domain.TestUser;
import com.shrona.line_demo.line.domain.LineUser;
import com.shrona.line_demo.user.domain.User;
import com.shrona.line_demo.user.domain.vo.PhoneNumber;
import java.util.Optional;

public record TestUserForm(
    Long id,
    String phoneNumber,
    String lineId
) {

    public static TestUserForm of(TestUser testUser) {
        String phoneNumber = Optional.ofNullable(testUser.getUser())
            .map(User::getPhoneNumber)
            .map(PhoneNumber::getPhoneNumber)
            .orElse(null);

        String lineId = Optional.ofNullable(testUser.getUser())
            .map(User::getLineUser)
            .map(LineUser::getLineId)
            .orElse(null);

        return new TestUserForm(
            testUser.getId(),
            phoneNumber,
            lineId
        );
    }

}
