package com.shrona.line_demo.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.shrona.line_demo.user.domain.Group;
import com.shrona.line_demo.user.domain.User;
import com.shrona.line_demo.user.domain.UserGroup;
import com.shrona.line_demo.user.domain.vo.PhoneNumber;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class UserGroupJpaRepositoryTest {

    @Autowired
    private UserJpaRepository userJpaRepository;


    @Autowired
    private UserGroupJpaRepository userGroupJpaRepository;

    @Autowired
    private GroupJpaRepository groupJpaRepository;

    @Test
    public void 그룹에_속한_유저수_조회() {

        // given
        Group group = Group.createGroup("그룹 1", "설명");
        Group group2 = Group.createGroup("그룹 2", "설명");

        User user1 = userJpaRepository.save(User.createUser(new PhoneNumber("010-1234-1234")));
        User user2 = userJpaRepository.save(User.createUser(new PhoneNumber("010-1234-1235")));
        User user3 = userJpaRepository.save(User.createUser(new PhoneNumber("010-1234-1236")));

        group.addUserToGroup(List.of(
            UserGroup.createUserGroup(user1, group),
            UserGroup.createUserGroup(user2, group))
        );

        group2.addUserToGroup(List.of(UserGroup.createUserGroup(user3, group2)));

        Group groupInfo = groupJpaRepository.save(group);
        Group groupInfo2 = groupJpaRepository.save(group2);

        // when
        List<Object[]> groupUserCounts = userGroupJpaRepository.countByGroupIds(
            List.of(groupInfo.getId(), groupInfo2.getId()));
        Map<Long, Long> groupIdToCount = groupUserCounts.stream()
            .collect(Collectors.toMap(
                arr -> (Long) arr[0],
                arr -> (Long) arr[1]
            ));

        // then
        assertThat(groupIdToCount.get(groupInfo.getId())).isEqualTo(2);
        assertThat(groupIdToCount.get(groupInfo2.getId())).isEqualTo(1);

    }

}