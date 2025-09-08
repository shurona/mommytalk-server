package com.shrona.mommytalk.user.infrastructure.repository;

import com.shrona.mommytalk.user.domain.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserQueryRepository {

    /**
     * 유저의 라인 아이디를 기준으로 유저 조회
     */
    User findUserByLineId(String lineId);

    /**
     * 유저의 목록을 갖고 온다.
     */
    List<?> findUserList(Long channelId);

    /**
     * 페이징을 사용해서 유저 목록 갖고 온다.
     */
    Page<User> findUserListWithPaging(Long channelId, Pageable pageable);
}
