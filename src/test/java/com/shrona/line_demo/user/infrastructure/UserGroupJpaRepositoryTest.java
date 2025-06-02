package com.shrona.line_demo.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.shrona.line_demo.line.domain.Channel;
import com.shrona.line_demo.line.domain.LineUser;
import com.shrona.line_demo.line.infrastructure.ChannelJpaRepository;
import com.shrona.line_demo.line.infrastructure.LineUserJpaRepository;
import com.shrona.line_demo.user.domain.Group;
import com.shrona.line_demo.user.domain.User;
import com.shrona.line_demo.user.domain.UserGroup;
import com.shrona.line_demo.user.domain.vo.PhoneNumber;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class UserGroupJpaRepositoryTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private LineUserJpaRepository lineUserJpaRepository;


    @Autowired
    private UserGroupJpaRepository userGroupJpaRepository;

    @Autowired
    private GroupJpaRepository groupJpaRepository;

    @Autowired
    private ChannelJpaRepository channelRepository;

    @PersistenceContext
    private EntityManager em;

    private Channel channel;

    @BeforeEach
    public void createUserForTest() {
        // 채널 정보 저장
        channel = channelRepository.save(Channel.createChannel("이름", "설명"));

    }

    @Test
    public void 그룹에_속한_유저수_조회() {

        // given
        Group group = Group.createGroup(channel, "그룹 1", "설명");
        Group group2 = Group.createGroup(channel, "그룹 2", "설명");

        LineUser lineUser = LineUser.createLineUser("lineId");
        LineUser lineUser2 = LineUser.createLineUser("lineId2");
        LineUser lineUser3 = LineUser.createLineUser("lineId3");

        lineUserJpaRepository.saveAll(List.of(lineUser3, lineUser2, lineUser));

        User user1 = userJpaRepository.save(
            User.createUserWithLine(new PhoneNumber("010-1234-1234"), lineUser));
        User user2 = userJpaRepository.save(
            User.createUserWithLine(new PhoneNumber("010-1234-1235"), lineUser2));
        User user3 = userJpaRepository.save(
            User.createUserWithLine(new PhoneNumber("010-1234-1236"), lineUser3));
        // 라인 정보 없이 추가
        User user4 = userJpaRepository.save(
            User.createUser(new PhoneNumber("010-1234-1237")));

        // 2개 추가
        group.addUserToGroup(List.of(
            UserGroup.createUserGroup(user1, group),
            UserGroup.createUserGroup(user2, group))
        );

        // 1개 추가
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