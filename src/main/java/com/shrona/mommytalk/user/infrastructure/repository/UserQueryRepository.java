package com.shrona.mommytalk.user.infrastructure.repository;

import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.infrastructure.dao.UserListProjection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserQueryRepository {

    /**
     * 유저의 라인 아이디를 기준으로 유저 조회
     */
    User findUserByLineId(String lineId);

    /**
     * LINE 채널에 속한 유저 조회 (팔로우 상태만)
     */
    List<UserListProjection> findLineUsersByChannelId(Long channelId);

    /**
     * LINE 채널에 속한 유저 조회 - 페이징 지원
     */
    Page<UserListProjection> findLineUsersByChannelIdWithPaging(Long channelId, Pageable pageable);

    /**
     * KAKAO 채널에 속한 유저 조회 (팔로우 상태만)
     */
    List<UserListProjection> findKakaoUsersByChannelId(Long channelId);

    /**
     * KAKAO 채널에 속한 유저 조회 - 페이징 지원
     */
    Page<UserListProjection> findKakaoUsersByChannelIdWithPaging(Long channelId, Pageable pageable);

    /**
     * User 아이디를 기준으로 유저 정보 조회
     */
    User findUserByUserId(Long userId);
}
