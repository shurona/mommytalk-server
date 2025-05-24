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
        Group groupInfo = groupRepository.save(Group.createGroup(name, description));

        // 휴대전화 목록에 맞는 유저를 group에 추가해준다.
        generateGroupUserInfo(groupInfo, phoneList);

        return groupRepository.findById(groupInfo.getId()).orElseThrow();
    }

    @Override
    public Group findGroupById(Long id, Boolean withUser) {
        Optional<Group> groupInfo;
        if (withUser) {
            groupInfo = groupRepository.findGroupWithUsers(id);
        } else {
            groupInfo = groupRepository.findById(id);
        }

        return groupInfo.orElse(null);
    }

    @Override
    public List<Group> findGroupByIdList(List<Long> ids) {

        return groupRepository.findAllById(ids);
    }

    @Override
    public Page<Group> findGroupList(Pageable pageable) {

        Page<Group> groupsPageList = groupRepository.findAll(pageable);

        for (Group group : groupsPageList.toList()) {
            for (UserGroup userGroup : group.getUserGroupList()) {
                userGroup.getUser();
            }
        }
        return groupsPageList;
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

        generateGroupUserInfo(groupInfo.get(), phoneNumberList);
    }

    @Transactional
    public void deleteGroup(List<Long> groupIdList) {
        if (groupIdList.isEmpty()) {
            return;
        }
        groupRepository.deleteAllById(groupIdList);
    }

    @Transactional
    public void softDeleteGroup(List<Long> groupIdList) {
        System.out.println(groupIdList);
        List<Group> groupList = groupRepository.findAllById(groupIdList);
        for (Group group : groupList) {
            for (UserGroup userGroup : group.getUserGroupList()) {
                userGroup.deleteUserGroup();
            }
            group.deleteGroup();
        }
    }

    @Transactional
    public void deleteUserFromGroupByPhones(Long id, List<String> phoneNumberList) {
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

    @Transactional
    public void deleteUserFromGroupByIds(Long id, List<Long> ids) {

        System.out.println(id + " : " + ids);

        Optional<Group> groupInfo = groupRepository.findById(id);
        if (groupInfo.isEmpty()) {
            return;
        }

        groupInfo.get().getUserGroupList().clear();

        System.out.println(ids);

        userGroupRepository.deleteAllById(ids);
    }

    /**
     * phoneNumber 목록을 조사해서 없는 번호는 입력해준다.
     */
    @Transactional
    private void generateGroupUserInfo(Group groupInfo, List<String> phoneNumberList) {
        // 입력된 전화번호를 유저 생성 및 라인 유저와 매칭 후 List 반환
        List<User> userListFromPhoneNumber = userService
            .findOrCreateUsersByPhoneNumbers(phoneNumberList);

        // group에 이미 존재하는 번호들을 추출한다.
        List<User> pList = groupInfo.getUserGroupList().stream()
            .map(UserGroup::getUser).toList();

        // 존재하는 번호들을 추출해낸다.
        Set<PhoneNumber> existPhoneNumbers = userUtils.extractPhoneNumbers(pList);

        // 유저 그룹에 없는 신규 유저를 저장한다.
        List<UserGroup> list = userListFromPhoneNumber.stream()
            .filter(u -> !existPhoneNumbers.contains(u.getPhoneNumber()))
            .map(nu -> UserGroup.createUserGroup(nu, groupInfo)).toList();

        groupInfo.addUserToGroup(list);

        // 단체 저장
        userGroupRepository.saveAll(list);
    }
}
