package com.shrona.mommytalk.user.application;

import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.infrastructure.repository.dao.UserListProjection;
import com.shrona.mommytalk.user.presentation.dtos.request.UpdateUserRequestDto;
import com.shrona.mommytalk.user.presentation.dtos.response.UserResponseDto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    /**
     * 유저 생성
     */
    User createUser(String phoneNumber);

    /**
     * 유저 엔티티만 단순 조회
     */
    Optional<User> findById(Long id);

    /**
     * 유저 정보를 id로 조회
     */
    UserResponseDto findUserInfoById(Long userId);

    /**
     * 유저 단일 조회
     */
    User findUserByPhoneNumber(String phoneNumber);

    /**
     * 라인 유저를 기준으로 유저조회
     */
    Optional<User> findUserByLineUser(LineUser lineUser);

    /**
     * 유저 목록 조회
     */
    List<User> findUserList();

    /**
     * 채널에 속한 유저 목록 갖고 온다.
     */
    Page<UserListProjection> findUserListByChannelInfoWithPaging(Long channelId, Pageable pageable);

    /**
     * 휴대전화 번호 입력을 기준으로 없는 유저는 생성 후 조회
     */
    List<User> findOrCreateUsersWithLinesByPhoneNumbers(List<String> phoneNumberList);

    /**
     * 유저 정보를 업데이트 해준다.
     */
    void updateUserInfoByRequest(Long userId, UpdateUserRequestDto requestDto);

    /**
     * 라인 유저의 휴대전화를 업데이트 한다. (만약 유저가 없으면 생성)
     */
    void updateUserPhoneNumberByLineUser(String lineId, String phoneNumber);

    /**
     * User 정보 삭제
     */
    void deleteUser(User user);

    /**
     * 휴대전화 번호를 기준으로 UserGroup과 User 정보 삭제
     */
    void deleteUserGroupAndUserInfo(String phoneNumber);
}
