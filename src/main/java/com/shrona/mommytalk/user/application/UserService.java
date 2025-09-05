package com.shrona.mommytalk.user.application;

import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.user.domain.User;
import java.util.List;
import java.util.Optional;

public interface UserService {

    /**
     * 유저 생성
     */
    public User createUser(String phoneNumber);
    
    /**
     * 유저를 id로 조회
     */
    public Optional<User> findById(Long id);

    /**
     * 유저 단일 조회
     */
    public User findUserByPhoneNumber(String phoneNumber);

    /**
     * 라인 유저를 기준으로 유저조회
     */
    public Optional<User> findUserByLineUser(LineUser lineUser);

    /**
     * 유저 목록 조회
     */
    public List<User> findUserList();

    /**
     * 휴대전화 번호 입력을 기준으로 없는 유저는 생성 후 조회
     */
    public List<User> findOrCreateUsersWithLinesByPhoneNumbers(List<String> phoneNumberList);

    /**
     * User 정보 삭제
     */
    public void deleteUser(User user);

    /**
     * 휴대전화 번호를 기준으로 UserGroup과 User 정보 삭제
     */
    public void deleteUserGroupAndUserInfo(String phoneNumber);
}
