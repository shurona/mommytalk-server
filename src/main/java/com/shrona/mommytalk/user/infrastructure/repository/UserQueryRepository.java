package com.shrona.mommytalk.user.infrastructure.repository;

import com.shrona.mommytalk.user.domain.User;

public interface UserQueryRepository {

    /**
     * 유저의 라인 아이디를 기준으로 유저 조회
     */
    User findUserByLineId(String lineId);
}
