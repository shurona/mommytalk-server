package com.shrona.mommytalk.user.application;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.infrastructure.repository.dao.UserListProjection;
import com.shrona.mommytalk.user.presentation.dtos.request.UpdateUserRequestDto;
import com.shrona.mommytalk.user.presentation.dtos.response.SentenceHistoryResponseDto;
import com.shrona.mommytalk.user.presentation.dtos.response.TokenUsageResponseDto;
import com.shrona.mommytalk.user.presentation.dtos.response.UserResponseDto;
import java.time.LocalTime;
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
    UserResponseDto findUserInfoById(Channel channel, Long userId);

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
    Page<UserListProjection> findUserListByChannelInfoWithPaging(
        Long channelId, Pageable pageable, String searchToken);

    /**
     * 휴대전화 번호 입력을 기준으로 없는 유저는 생성 후 조회
     */
    List<User> findOrCreateUsersByPhoneNumbers(List<String> phoneNumberList);

    /**
     * 유저 정보를 업데이트 해준다.(어드민)
     */
    void updateUserInfoByAdmin(Long userId, UpdateUserRequestDto requestDto);

    /**
     * 유저 정보를 업데이트 해준다.(클라이언트)
     */
    void updateUserInfoByClient(Long userId, UpdateUserRequestDto requestDto);

    /**
     * 카카오 메시지 선호 발송 시간(KST)을 다음날부터 적용되도록 pending에 저장한다.
     */
    User updatePreferredSendTime(Long userId, LocalTime preferredSendTime);

    /**
     * 대기 중인 선호 발송 시간(pending)을 일괄 승격하고 승격 건수를 반환한다.
     */
    int promotePendingPreferredSendTime();

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

    /**
     * 채널별 유저 문장 이력 조회 (관리자용)
     * - 선택적 날짜 필터링 (year, month, day)
     * - 선택적 유저 필터링 (userId)
     * - 페이징 지원
     */
    Page<SentenceHistoryResponseDto> findSentenceHistoryByChannel(
        Long channelId,
        Integer year,
        Integer month,
        Integer day,
        Long userId,
        Pageable pageable
    );

    /**
     * 채널별 토큰 사용량 합계 조회 (관리자용)
     * - 선택적 날짜 필터링 (year, month, day)
     * - 선택적 유저 필터링 (userId)
     */
    TokenUsageResponseDto getTokenUsageSumByChannel(
        Long channelId,
        Integer year,
        Integer month,
        Integer day,
        Long userId
    );
}
