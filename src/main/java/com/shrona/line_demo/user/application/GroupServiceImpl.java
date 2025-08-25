package com.shrona.line_demo.user.application;

import com.shrona.line_demo.line.domain.Channel;
import com.shrona.line_demo.user.common.utils.UserUtils;
import com.shrona.line_demo.user.domain.Group;
import com.shrona.line_demo.user.domain.User;
import com.shrona.line_demo.user.domain.UserGroup;
import com.shrona.line_demo.user.domain.vo.PhoneNumber;
import com.shrona.line_demo.user.infrastructure.GroupJpaRepository;
import com.shrona.line_demo.user.infrastructure.UserGroupJpaRepository;
import com.shrona.line_demo.user.infrastructure.dao.GroupUserCount;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
    public Group createGroup(
        Channel channel, String name, String description, List<String> phoneList) {
        Group groupInfo = groupRepository.save(Group.createGroup(channel, name, description));

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
    public Page<UserGroup> findUserGroupByGroupId(Group group, Pageable pageable) {
        return userGroupRepository.findAllByGroupId(group, pageable);
    }

    @Override
    public List<String> findLineIdsByGroupIds(List<Long> groupList) {
        return groupRepository.findLineIdsByGroupIds(groupList);
    }

    @Override
    public List<Group> findGroupListNotIn(Channel channel, List<Long> exceptGroupIds) {
        if (exceptGroupIds == null || exceptGroupIds.isEmpty()) {
            // 제외할 ID가 없으면 전체 그룹 조회
            return groupRepository.findAllByChannel(channel);
        }
        return groupRepository.findByChannelAndIdNotIn(channel, exceptGroupIds);
    }

    @Override
    public Page<Group> findGroupList(Channel channel, Pageable pageable) {

        return groupRepository.findAllByChannel(channel, pageable);
    }

    @Override
    public Map<Long, Integer> findGroupLineUserCount(List<Long> groupIds) {

        List<GroupUserCount> groupUserCounts = userGroupRepository.countByGroupIds(groupIds);

        return groupUserCounts.stream()
            .collect(Collectors.toMap(
                GroupUserCount::groupId,
                arr -> arr.ct().intValue()
            ));
    }

    @Override
    public Map<Long, Integer> findGroupAllUserCount(List<Long> groupIds) {
        List<GroupUserCount> groupUserCounts = userGroupRepository.countAllUsersByGroupIds(
            groupIds);

        return groupUserCounts.stream()
            .collect(Collectors.toMap(
                GroupUserCount::groupId,
                arr -> arr.ct().intValue()
            ));
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
        Optional<Group> groupInfo = groupRepository.findById(id);
        if (groupInfo.isEmpty()) {
            return;
        }

        groupInfo.get().getUserGroupList().clear();
        userGroupRepository.deleteAllById(ids);
    }

    /**
     * 유저가 변경 될 때 UserGroup의 User를  source -> target으로 변경해준다.
     */
    @Transactional
    public void mergeUserGroupBeforeToAfter(User source, User target) {

        // 변경 이후가 될 UserGroup 목록
        List<UserGroup> targetUserGroupList = userGroupRepository.findAllByUser(target);

        // 변경이 될 유저 그룹 목록
        List<UserGroup> sourceUserGroupList = userGroupRepository.findAllByUser(source);

        // 변경 목표인 user의 UserGroup 목록을 갖고 온다.
        Set<Long> conflictIds = targetUserGroupList.stream()
            .map(g -> g.getGroup().getId())
            .collect(Collectors.toSet());

        // 겹치지 않는 afterUserGroup의 User를 after User로 변경해준다.
        if (target != null) {
            sourceUserGroupList.stream()
                .filter(ug -> !conflictIds.contains(ug.getGroup().getId()))
                .forEach(ug -> ug.changeUser(target));
        }

        // 겹치는 afterGroupUser는 삭제해준다.
        List<Long> deleteUGIds = sourceUserGroupList.stream()
            .filter(ug -> conflictIds.contains(ug.getGroup().getId()))
            .map(UserGroup::getId).toList();

        // 초기화
        userGroupRepository.deleteAllById(deleteUGIds);

        // beforeUser의 User는 삭제처리한다.
        if (source != null) {
            source.clearLineUserAndPhoneNumber();
            userService.deleteUser(source);
        }
    }

    /**
     * phoneNumber 목록을 조사해서 없는 번호는 입력해준다.
     */
    @Transactional
    private void generateGroupUserInfo(Group groupInfo, List<String> phoneNumberList) {
        // 입력된 전화번호를 유저 생성 및 라인 유저와 매칭 후 List 반환
        List<User> userListFromPhoneNumber = userService
            .findOrCreateUsersWithLinesByPhoneNumbers(phoneNumberList);

        // group에 이미 존재하는 번호들을 추출한다.
        List<User> pList = groupInfo.getUserGroupList().stream()
            .map(UserGroup::getUser).toList();

        // 존재하는 번호들을 추출해낸다.
        Set<PhoneNumber> existPhoneNumbers = userUtils.extractPhoneNumbers(pList);

        // 유저 그룹에 없는 유저를 UserGroup에 추가해준다.
        List<UserGroup> list = userListFromPhoneNumber.stream()
            .filter(u -> !existPhoneNumbers.contains(u.getPhoneNumber()))
            .map(nu -> UserGroup.createUserGroup(nu, groupInfo)).toList();
        groupInfo.addUserToGroup(list);

        // 단체 저장
        userGroupRepository.saveAll(list);
    }
}
