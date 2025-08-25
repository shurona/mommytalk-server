package com.shrona.mommytalk.user.common.utils;

import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserUtils {

    // Users의 휴대전화를 Set형식으로 변환
    public Set<PhoneNumber> extractPhoneNumbers(List<User> users) {
        return users.stream()
            .map(User::getPhoneNumber)
            .collect(Collectors.toSet());
    }

}
