package com.shrona.line_demo.user.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shrona.line_demo.user.domain.Group;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class GroupServiceImplTest {

    @Autowired
    private GroupServiceImpl groupService;

    @Autowired
    private UserServiceImpl userService;

    @PersistenceContext
    private EntityManager em;

    @Test
    public void 그룹_생성_테스트() {
        // given
        String name = "그룹 이름";
        String description = "그룹 설명";

        String one = "010-2222-3333";
        String two = "010-3123-1231";
        String unCorrect = "02-322-3232";

        // when
        Group group = groupService.createGroup(name, description, List.of(one, two, unCorrect));

        // then
        assertThat(group.getName()).isEqualTo(name);
        assertThat(group.getUserGroupList().size()).isEqualTo(2);
    }

    @Test
    public void 그룹_삭제_테스트() {
        // given
        String name = "그룹 이름";
        String description = "그룹 설명";

        String one = "010-2222-3333";
        String two = "010-3123-1231";
        String unCorrect = "02-322-3232";

        // when
        Group group = groupService.createGroup(name, description, List.of(one, two, unCorrect));
        groupService.deleteGroup(List.of(group.getId()));
        group = groupService.findGroupById(group.getId());

        // then
        assertThat(group.getIsDeleted()).isFalse();
    }

    @Test
    public void 그룹에_유저추가_테스트() {
        // given
        String name = "그룹 이름";
        String description = "그룹 설명";

        String one = "010-2222-3333";
        String two = "010-3123-1231";
        String wrongInfo = "01-2022-1234";

        // 먼저 그룹 생성
        Group group = groupService.createGroup(name, description, List.of(one));

        // 유저를 미리 저장
        userService.createUser(two);

        // when
        groupService.addUserToGroup(group.getId(), List.of(one, two, wrongInfo));

        // 반영
        em.flush();
        em.clear();
        Group afterAdd = groupService.findGroupById(group.getId());

        // then
        assertThat(afterAdd.getUserGroupList().size()).isEqualTo(2);

    }

}