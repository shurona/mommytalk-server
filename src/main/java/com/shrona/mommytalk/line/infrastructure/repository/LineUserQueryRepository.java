package com.shrona.mommytalk.line.infrastructure.repository;

import com.shrona.mommytalk.line.domain.LineUser;

public interface LineUserQueryRepository {

    LineUser findLineUserByUserId(Long userId);

}
