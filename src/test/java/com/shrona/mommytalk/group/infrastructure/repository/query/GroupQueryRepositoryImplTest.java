package com.shrona.mommytalk.group.infrastructure.repository.query;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.config.JpaTestConfig;
import com.shrona.mommytalk.group.domain.Group;
import com.shrona.mommytalk.group.domain.UserGroup;
import com.shrona.mommytalk.group.infrastructure.repository.jpa.GroupJpaRepository;
import com.shrona.mommytalk.group.infrastructure.repository.jpa.UserGroupJpaRepository;
import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.line.infrastructure.repository.jpa.ChannelJpaRepository;
import com.shrona.mommytalk.line.infrastructure.repository.jpa.LineUserJpaRepository;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import com.shrona.mommytalk.user.infrastructure.repository.jpa.UserJpaRepository;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import({JpaTestConfig.class, GroupQueryRepositoryImpl.class})
@DataJpaTest
public class GroupQueryRepositoryImplTest {

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

    @Autowired
    private GroupQueryRepositoryImpl groupQueryRepository;

    private Channel channel;

    @BeforeEach
    public void createUserForTest() {

        // 채널 정보 저장
        channel = channelRepository.save(Channel.createChannel("이름", "설명"));

    }

    @Test
    public void 그룹아이기준으로_유저정보_조회() {

        // given
        Group group = Group.createGroup(channel, "그룹 1", "설명");

        Group group2 = Group.createGroup(channel, "그룹 2", "설명");

        LineUser lineUser = LineUser.createLineUser("lineId");
        LineUser lineUser2 = LineUser.createLineUser("lineId2");

        lineUserJpaRepository.saveAll(List.of(lineUser2, lineUser));

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
        group2.addUserToGroup(
            List.of(
                UserGroup.createUserGroup(user1, group2),
                UserGroup.createUserGroup(user2, group2)
            )
        );
        List<Group> groups = groupJpaRepository.saveAll(List.of(group, group2));

        // when
        List<User> userListByGroupIds = groupQueryRepository.findUserListByGroupIds(
            groups.stream().map(Group::getId).toList()
        );

        // then
        Assertions.assertThat(userListByGroupIds.size()).isEqualTo(3);
    }

}