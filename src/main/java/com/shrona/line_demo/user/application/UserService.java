package com.shrona.line_demo.user.application;

import com.shrona.line_demo.user.domain.User;
import java.util.List;

public interface UserService {


    /**
     * 유저 생성
     */
    public User createUser(String phoneNumber);

    /**
     * 유저 단일 조회
     */
    public User findUserByPhoneNumber(String phoneNumber);

    /**
     * 유저 목록 조회
     */
    public List<User> findUserList();

    /**
     * 휴대전화 번호 입력을 기준으로 없는 유저는 생성 후 조회
     */
    public List<User> findOrCreateUsersByPhoneNumbers(List<String> phoneNumberList);

}
