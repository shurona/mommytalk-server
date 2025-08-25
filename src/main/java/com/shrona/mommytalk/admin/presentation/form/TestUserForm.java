package com.shrona.mommytalk.admin.presentation.form;

import com.shrona.mommytalk.admin.domain.TestUser;
import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
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
