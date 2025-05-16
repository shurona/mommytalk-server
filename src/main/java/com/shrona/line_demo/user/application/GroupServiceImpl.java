package com.shrona.line_demo.user.application;

import com.shrona.line_demo.user.common.utils.UserUtils;
import com.shrona.line_demo.user.domain.Group;
import com.shrona.line_demo.user.domain.User;
import com.shrona.line_demo.user.domain.UserGroup;
import com.shrona.line_demo.user.domain.vo.PhoneNumber;
import com.shrona.line_demo.user.infrastructure.GroupJpaRepository;
import com.shrona.line_demo.user.infrastructure.UserGroupJpaRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class GroupServiceImpl implements GroupService {

    // jpa
    private final GroupJpaRepository groupRepository;
    private final UserGroupJpaRepository userGroupRepository;

    // service
    private final UserService userService;

    // utils
    private final UserUtils userUtils;

    @Transactional
    public Group createGroup(String name, String description, List<String> phoneList) {

        List<User> userList = userService.findOrCreateUsersByPhoneNumbers(phoneList);

        Group group = Group.createGroup(name, description);
        group.addUserToGroup(userList);

        return groupRepository.save(group);
    }

    @Override
    public Group findGroupById(Long id) {
        Optional<Group> groupInfo = groupRepository.findById(id);

        return groupInfo.orElse(null);
    }

    @Override
    public Page<Group> findGroupList(Pageable pageable) {

        return groupRepository.findAll(pageable);
    }

    @Transactional
    public Group updateGroupInfo(Long groupId, String newName, String newDescription) {
        Optional<Group> groupInfo = groupRepository.findById(groupId);
        if (groupInfo.isEmpty()) {
            return null;
        }

        groupInfo.get().updateGroupInfo(newName, newDescription);

        return groupInfo.get();
    }

    @Transactional
    public void addUserToGroup(Long groupId, List<String> phoneNumberList) {
        Optional<Group> groupInfo = groupRepository.findGroupWithUsers(groupId);
        if (groupInfo.isEmpty()) {
            return;
        }

        // 입력된 전화번호를 유저 생성 및 라인 유저와 매칭 후 List 반환
        List<User> userListFromPhoneNumber = userService
            .findOrCreateUsersByPhoneNumbers(phoneNumberList);

        // group에 이미 존재하는 번호들을 추출한다.
        List<User> pList = groupInfo.get().getUserGroupList().stream()
            .map(UserGroup::getUser).toList();

        // 존재하는 번호들을 추출해낸다.
        Set<PhoneNumber> existPhoneNumbers = userUtils.extractPhoneNumbers(pList);

        // 유저 그룹에 없는 신규 유저를 저장한다.
        List<UserGroup> list = userListFromPhoneNumber.stream()
            .filter(u -> !existPhoneNumbers.contains(u.getPhoneNumber()))
            .map(nu -> UserGroup.createUserGroup(nu, groupInfo.get())).toList();

        // 단체 저장
        userGroupRepository.saveAll(list);
    }

    @Transactional
    public void deleteGroup(List<Long> groupIdList) {
        List<Group> groupList = groupRepository.findAllById(groupIdList);
        for (Group group : groupList) {
            group.deleteGroup();
        }
    }

    @Transactional
    public void deleteUserFromGroup(Long id, List<String> phoneNumberList) {
        Optional<Group> groupInfo = groupRepository.findGroupWithUsers(id);
        if (groupInfo.isEmpty()) {
            return;
        }

        Set<String> phoneSet = new HashSet<>(phoneNumberList);

        List<Long> ids = groupInfo.get().getUserGroupList()
            .stream()
            .filter(ug -> phoneSet.contains(ug.getUser().getPhoneNumber().getPhoneNumber()))
            .map(UserGroup::getId)
            .toList();

        groupInfo.get().getUserGroupList().clear();

        userGroupRepository.deleteAllById(ids);
    }
}
