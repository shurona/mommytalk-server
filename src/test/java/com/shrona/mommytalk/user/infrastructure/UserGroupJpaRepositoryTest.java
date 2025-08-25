package com.shrona.mommytalk.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.shrona.mommytalk.line.domain.Channel;
import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.line.infrastructure.ChannelJpaRepository;
import com.shrona.mommytalk.line.infrastructure.LineUserJpaRepository;
import com.shrona.mommytalk.user.domain.Group;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.UserGroup;
import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import com.shrona.mommytalk.user.infrastructure.dao.GroupUserCount;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

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
        User user5 = userJpaRepository.save(
            User.createUser(new PhoneNumber("010-1234-1238")));

        // 2개 추가
        group.addUserToGroup(List.of(
            UserGroup.createUserGroup(user1, group), // 라인 있음
            UserGroup.createUserGroup(user2, group),
            UserGroup.createUserGroup(user4, group)) // 라인 없음
        );

        // 1개 추가
        group2.addUserToGroup(
            List.of(
                UserGroup.createUserGroup(user3, group2), // 라인 있음
                UserGroup.createUserGroup(user4, group2), // 라인 없음
                UserGroup.createUserGroup(user5, group2)
            )
        );

        Group groupInfo = groupJpaRepository.save(group);
        Group groupInfo2 = groupJpaRepository.save(group2);

        // when
        List<GroupUserCount> groupUserCounts = userGroupJpaRepository.countByGroupIds(
            List.of(groupInfo.getId(), groupInfo2.getId()));
        Map<Long, Long> groupIdToCount = groupUserCounts.stream()
            .collect(Collectors.toMap(
                GroupUserCount::groupId,
                GroupUserCount::ct
            ));

        List<GroupUserCount> groupAllUserCounts = userGroupJpaRepository.countAllUsersByGroupIds(
            List.of(groupInfo.getId(), groupInfo2.getId()));

        Map<Long, Long> allUserGroupIdToCount = groupAllUserCounts.stream()
            .collect(Collectors.toMap(
                GroupUserCount::groupId,
                GroupUserCount::ct
            ));

        // then
        assertThat(groupIdToCount.get(groupInfo.getId())).isEqualTo(2);
        assertThat(groupIdToCount.get(groupInfo2.getId())).isEqualTo(1);

        assertThat(allUserGroupIdToCount.get(groupInfo.getId())).isEqualTo(3);
        assertThat(allUserGroupIdToCount.get(groupInfo2.getId())).isEqualTo(3);

    }

    @Test
    public void 유저그룹_조회테스트() {

        // given
        Group group = Group.createGroup(channel, "그룹 1", "설명");

        LineUser lineUser = LineUser.createLineUser("lineId");
        LineUser lineUser2 = LineUser.createLineUser("lineId2");
        LineUser lineUser3 = LineUser.createLineUser("lineId3");

        lineUserJpaRepository.saveAll(List.of(lineUser3, lineUser2, lineUser));

        User user1 = userJpaRepository.save(
            User.createUserWithLine(new PhoneNumber("010-1234-1234"), lineUser));
        User user2 = userJpaRepository.save(
            User.createUserWithLine(new PhoneNumber("010-1234-1235"), lineUser2));
        // 라인 정보 없이 추가
        User user4 = userJpaRepository.save(
            User.createUser(new PhoneNumber("010-1234-1237")));

        // 2개 추가
        group.addUserToGroup(List.of(
            UserGroup.createUserGroup(user1, group), // 라인 있음
            UserGroup.createUserGroup(user2, group),
            UserGroup.createUserGroup(user4, group)) // 라인 없음
        );
        Group groupInfo = groupJpaRepository.save(group);

        // when
        Page<UserGroup> allByGroupId = userGroupJpaRepository.findAllByGroupId(groupInfo,
            PageRequest.of(0, 2));

        // then
        assertThat(allByGroupId.getTotalPages()).isEqualTo(2);
        assertThat(allByGroupId.getTotalElements()).isEqualTo(3);

    }
}