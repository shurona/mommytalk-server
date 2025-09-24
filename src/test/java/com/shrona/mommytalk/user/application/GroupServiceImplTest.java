package com.shrona.mommytalk.user.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.group.application.GroupServiceImpl;
import com.shrona.mommytalk.group.domain.Group;
import com.shrona.mommytalk.group.domain.UserGroup;
import com.shrona.mommytalk.line.infrastructure.repository.jpa.ChannelJpaRepository;
import com.shrona.mommytalk.user.domain.User;
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

    User userOne;
    User userTwo;
    String phoneOne = "010-2222-3333";
    String phoneTwo = "010-3123-1231";
    @Autowired
    private GroupServiceImpl groupService;
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private ChannelJpaRepository channelRepository;
    @PersistenceContext
    private EntityManager em;
    private Channel channel;

    @BeforeEach
    public void createUserForTest() {
        userOne = userService.createUser(phoneOne);
        userTwo = userService.createUser(phoneTwo);

        // 채널 정보 저장
        channel = channelRepository.save(Channel.createChannel("이름", "설명"));

    }

    @Test
    public void 그룹_생성_테스트() {
        // given
        String name = "그룹 이름";
        String description = "그룹 설명";

        String one = phoneOne;
        String two = phoneTwo;
        String unCorrect = "02-322-3232";

        // when
        Group aa = groupService.createGroup(
            channel, name, description, List.of(one, two, unCorrect));

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

        String one = phoneOne;
        String two = phoneTwo;
        String wrongInfo = "01-2022-1234";

        // 먼저 그룹 생성
        Group group = groupService.createGroup(channel, name, description, List.of(one));

        // 유저를 미리 저장
        userService.createUser(two);

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

        String one = phoneOne;

        // 먼저 그룹 생성
        Group group = groupService.createGroup(
            channel, name, description, List.of(one));

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

        String one = phoneOne;
        String two = phoneTwo;
        String unCorrect = "02-322-3232";

        // when
        Group group = groupService.createGroup(
            channel, name, description, List.of(one, two, unCorrect));
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

        String one = phoneOne;
        String two = phoneTwo;
        String three = "010-3123-1235";
        String wrongInfo = "01-2022-1234";

        // 먼저 그룹 생성
        Group group = groupService.createGroup(channel, name, description,
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

        String one = phoneOne;
        String two = phoneTwo;
        String three = "010-3123-1235";
        String wrongInfo = "01-2022-1234";

        // 먼저 그룹 생성
        Group group = groupService.createGroup(channel, name, description,
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

        String one = phoneOne;
        String two = phoneTwo;
        String three = "010-3123-1235";
        String wrongInfo = "01-2022-1234";

        // 먼저 그룹 생성
        Group group = groupService.createGroup(channel, name, description,
            List.of(one, two, two, three, wrongInfo));

        List<Long> ids = group.getUserGroupList().stream().map(UserGroup::getId).toList();

        // when
        groupService.deleteUserFromGroupByIds(group.getId(), ids);

        Group afterDelete = groupService.findGroupById(group.getId(), false);

        // then
        assertThat(afterDelete.getUserGroupList().size()).isEqualTo(0);
    }


    @DisplayName("유저를 옮기면 UserGroup의 병합 잘 되는지 테스트")
    @Test
    public void mergeUserGroup() {
        // given
        Group group = groupService.createGroup(
            channel, "name", "description", List.of(phoneOne, phoneTwo));
        Group groupTwo = groupService.createGroup(
            channel, "name-2", "description-2", List.of(phoneOne));
        Group groupThree = groupService.createGroup(
            channel, "name-3", "description-3", List.of(phoneOne));

        // 영속성 삭제
        em.flush();
        em.clear();

        // 영속성에 재등록
        User checkUser = userService.findUserByPhoneNumber(phoneOne);

        // when
        groupService.mergeUserGroupBeforeToAfter(checkUser, userTwo);

        // then
        Group groupInfoOne = groupService.findGroupById(group.getId(), true);
        Group groupInfoTwo = groupService.findGroupById(groupTwo.getId(), true);
        Group groupInfoThree = groupService.findGroupById(groupThree.getId(), true);

        List<UserGroup> ugTwoList = groupInfoTwo.getUserGroupList();
        List<UserGroup> ugThreeList = groupInfoThree.getUserGroupList();

        assertThat(groupInfoOne.getUserGroupList().size()).isEqualTo(1);
        assertThat(ugTwoList.size()).isEqualTo(1);
        // 다른 그룹들이 변경 되었는 지 확인
        assertThat(ugTwoList.get(0).getUser().getPhoneNumber().getPhoneNumber())
            .isEqualTo(phoneTwo);
        assertThat(ugThreeList.size()).isEqualTo(1);
        assertThat(ugThreeList.get(0).getUser().getPhoneNumber().getPhoneNumber())
            .isEqualTo(phoneTwo);

        // 유저 삭제 확인
        checkUser = userService.findUserByPhoneNumber(phoneOne);
        assertThat(checkUser).isNull();

    }
}