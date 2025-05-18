package com.shrona.line_demo.user.application;

import com.shrona.line_demo.user.domain.Group;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupService {

    public Group createGroup(String name, String description, List<String> phoneList);

    /**
     * Group 정보를 단일로 조회 user와 함께 fetch join을 할지 파라미터로 설정
     */
    public Group findGroupById(Long id, Boolean withUser);

    /**
     * 그룹 아이디에 해당하는 그룹 목록 조회
     */
    public List<Group> findGroupByIdList(List<Long> id);

    public Page<Group> findGroupList(Pageable pageable);

    public void addUserToGroup(Long groupId, List<String> phoneNumberList);

    public void deleteGroup(List<Long> groupIdList);

    Group updateGroupInfo(Long groupId, String newName, String newDescription);

    void deleteUserFromGroup(Long id, List<String> phoneNumberList);
}
