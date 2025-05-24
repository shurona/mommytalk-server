package com.shrona.line_demo.user.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shrona.line_demo.user.domain.Group;
import com.shrona.line_demo.user.domain.UserGroup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    @BeforeEach
    public void createUserForTest() {
        userService.createUser("010-2222-3333");
        userService.createUser("010-3123-1231");

    }

    @Test
    public void 그룹_생성_테스트() {
        // given
        String name = "그룹 이름";
        String description = "그룹 설명";

        String one = "010-2222-3333";
        String two = "010-3123-1231";
        String unCorrect = "02-322-3232";

        // when
        Group aa = groupService.createGroup(name, description, List.of(one, two, unCorrect));

        // 반영
        em.flush();
        em.clear();
        Group group = groupService.findGroupById(aa.getId(), false);

        // then
        assertThat(group.getName()).isEqualTo(name);
        assertThat(group.getUserGroupList().size()).isEqualTo(2);
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
        // 반영

        // when
        groupService.addUserToGroup(group.getId(), List.of(one, two, wrongInfo));

        // 반영
        em.flush();
        em.clear();
        Group afterAdd = groupService.findGroupById(group.getId(), false);

        // then
        assertThat(afterAdd.getUserGroupList().size()).isEqualTo(2);

    }

    @DisplayName("그룹 정보 수정")
    @Test
    public void 그룹_정보_수정() {
        // given
        String name = "그룹 이름";
        String description = "그룹 설명";

        String newName = "새 그룹";
        String newDescription = "";

        String one = "010-2222-3333";

        // 먼저 그룹 생성
        Group group = groupService.createGroup(name, description, List.of(one));

        // when
        Group groupUpdate = groupService.updateGroupInfo(group.getId(), newName, newDescription);

        // then
        assertThat(groupUpdate.getName()).isEqualTo(newName);
        assertThat(groupUpdate.getDescription()).isEqualTo(description);

    }

    @DisplayName("그룹 소프트 삭제")
    @Test
    public void 그룹_소프트_삭제_테스트() {
        // given
        String name = "그룹 이름";
        String description = "그룹 설명";

        String one = "010-2222-3333";
        String two = "010-3123-1231";
        String unCorrect = "02-322-3232";

        // when
        Group group = groupService.createGroup(name, description, List.of(one, two, unCorrect));
        groupService.softDeleteGroup(List.of(group.getId()));
        group = groupService.findGroupById(group.getId(), false);

        // then
        assertThat(group.getIsDeleted()).isTrue();
    }

    @DisplayName("그룹 영구 삭제")
    @Test
    public void 그룹_삭제() {
        // given
        String name = "그룹 이름";
        String description = "그룹 설명";

        String one = "010-2222-3333";
        String two = "010-3123-1231";
        String three = "010-3123-1235";
        String wrongInfo = "01-2022-1234";

        // 먼저 그룹 생성
        Group group = groupService.createGroup(name, description,
            List.of(one, two, two, three, wrongInfo));

        // when
        groupService.deleteGroup(List.of(group.getId()));

        // 반영
        Group afterDelete = groupService.findGroupById(group.getId(), false);

        // then
        assertThat(afterDelete).isNull();
    }


    @DisplayName("그룹에 속한 유저들을 휴대전화 기준으로 삭제")
    @Test
    public void 그룹에_속한_유저들_삭제() {
        // given
        String name = "그룹 이름";
        String description = "그룹 설명";

        String one = "010-2222-3333";
        String two = "010-3123-1231";
        String three = "010-3123-1235";
        String wrongInfo = "01-2022-1234";

        // 먼저 그룹 생성
        Group group = groupService.createGroup(name, description,
            List.of(one, two, two, three, wrongInfo));

        // when
        groupService.deleteUserFromGroupByPhones(group.getId(), List.of(one, two));

        // 반영
        em.flush();
        em.clear();
        Group afterDelete = groupService.findGroupById(group.getId(), false);

        // then
        assertThat(afterDelete.getUserGroupList().size()).isEqualTo(1);
    }

    @DisplayName("그룹에 속한 유저들을 DB 아이디 기준으로 삭제")
    @Test
    public void 그룹에_속한_유저들_아이디기준_삭제() {
        // given
        String name = "그룹 이름";
        String description = "그룹 설명";

        String one = "010-2222-3333";
        String two = "010-3123-1231";
        String three = "010-3123-1235";
        String wrongInfo = "01-2022-1234";

        // 먼저 그룹 생성
        Group group = groupService.createGroup(name, description,
            List.of(one, two, two, three, wrongInfo));

        List<Long> ids = group.getUserGroupList().stream().map(UserGroup::getId).toList();

        // when
        groupService.deleteUserFromGroupByIds(group.getId(), ids);

        Group afterDelete = groupService.findGroupById(group.getId(), false);

        // then
        assertThat(afterDelete.getUserGroupList().size()).isEqualTo(0);
    }
}