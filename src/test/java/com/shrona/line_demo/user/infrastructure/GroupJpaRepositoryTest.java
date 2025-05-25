package com.shrona.line_demo.user.infrastructure;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.shrona.line_demo.user.domain.Group;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class GroupJpaRepositoryTest {

    @Autowired
    private GroupJpaRepository groupJpaRepository;

    @Test
    public void 특정아이디_제외하고_그룹조회() {

        // given
        Group group1 = Group.createGroup("그룹 1", "설명");
        Group group2 = Group.createGroup("그룹 2", "설명");
        Group group3 = Group.createGroup("그룹 3", "설명");

        List<Group> groups = groupJpaRepository.saveAll(List.of(group1, group2, group3));

        List<Long> list = groups.stream().map(Group::getId).toList();

        // when
        List<Group> groupList = groupJpaRepository.findByIdNotIn(
            List.of(list.get(0), list.get(1)));

        // then
        assertThat(groupList.size()).isEqualTo(1);
        assertThat(groupList.getFirst().getId()).isEqualTo(list.get(2));
    }

}