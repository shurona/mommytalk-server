package com.shrona.line_demo.user.application;

import com.shrona.line_demo.user.domain.Group;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupService {

    /**
     * 그룹 생성
     */
    public Group createGroup(String name, String description, List<String> phoneList);

    /**
     * Group 정보를 단일로 조회 user와 함께 fetch join을 할지 withUser 파라미터로 설정
     */
    public Group findGroupById(Long id, Boolean withUser);

    /**
     * 그룹 아이디에 해당하는 그룹 목록 조회
     */
    public List<Group> findGroupByIdList(List<Long> id);

    /**
     * 그룹 목록 조회
     */
    public Page<Group> findGroupList(Pageable pageable);

    /**
     * 그룹에 전화번호 추가
     */
    public void addUserToGroup(Long groupId, List<String> phoneNumberList);

    /**
     * 그룹 목록(영구) 삭제
     */
    public void deleteGroup(List<Long> groupIdList);

    /**
     * 그룹 정보 업데이트(이름, 설명만)
     */
    Group updateGroupInfo(Long groupId, String newName, String newDescription);

    /**
     * 그룹에서 휴대 전화를 기준으로 유저 목록 삭제
     */
    void deleteUserFromGroupByPhones(Long id, List<String> phoneNumberList);

    /**
     * 그룹에서 데이터베이스 아이디를 기준으로 유저 목록 삭제
     */
    void deleteUserFromGroupByIds(Long id, List<Long> ids);
}
