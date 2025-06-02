package com.shrona.line_demo.admin.presentation.form;

import com.shrona.line_demo.admin.domain.TestUser;

public record TestUserForm(
    Long id,
    String phoneNumber,
    String lineId
) {

    public static TestUserForm of(TestUser testUser) {
        String number = testUser.getLineUser().getPhoneNumber() == null ? "" :
            testUser.getLineUser().getPhoneNumber().getPhoneNumber();
        return new TestUserForm(
            testUser.getId(),
            number,
            testUser.getLineUser().getLineId()
        );
    }

}
