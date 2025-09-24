package com.shrona.mommytalk.line.infrastructure.repository.query;

import com.shrona.mommytalk.line.domain.LineUser;

public interface LineUserQueryRepository {

    LineUser findLineUserByUserId(Long userId);

}
