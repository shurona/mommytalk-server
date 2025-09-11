package com.shrona.mommytalk.user.infrastructure;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.group.domain.Group;
import com.shrona.mommytalk.group.infrastructure.repository.GroupJpaRepository;
import com.shrona.mommytalk.line.infrastructure.ChannelJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class GroupJpaRepositoryTest {

    @Autowired
    private GroupJpaRepository groupJpaRepository;

    @Autowired
    private ChannelJpaRepository channelRepository;

    @PersistenceContext
    private EntityManager em;

    private Channel channel;
    private Channel channelTwo;

    @BeforeEach
    public void createUserForTest() {

        // 채널 정보 저장
        channel = channelRepository.save(Channel.createChannel("이름", "설명"));
        channelTwo = channelRepository.save(Channel.createChannel("이름2", "설명"));

    }

    @Test
    public void 특정아이디_제외하고_그룹조회() {

        // given
        Group group1 = Group.createGroup(channel, "그룹 1", "설명");
        Group group2 = Group.createGroup(channel, "그룹 2", "설명");
        Group group3 = Group.createGroup(channel, "그룹 3", "설명");
        Group group4 = Group.createGroup(channelTwo, "그룹 4", "설명");

        List<Group> groups = groupJpaRepository.saveAll(List.of(group1, group2, group3, group4));

        List<Long> list = groups.stream().map(Group::getId).toList();

        // when
        List<Group> groupList = groupJpaRepository.findByChannelAndIdNotIn(
            channel, List.of(list.get(0), list.get(1)));

        // then
        assertThat(groupList.size()).isEqualTo(1);
        assertThat(groupList.getFirst().getId()).isEqualTo(list.get(2));
    }

}