package com.shrona.mommytalk.entitlement.application;

import com.shrona.mommytalk.entitlement.domain.Entitlement;
import com.shrona.mommytalk.entitlement.presentation.dtos.request.CreateEntitlementRequestDto;
import java.util.List;

public interface EntitlementService {

    /**
     * 모든 상품권 목록 조회
     */
    List<Entitlement> findAllEntitlements();

    /**
     * 상품 생성 및 모든 채널에 AUTO_ACTIVE, AUTO_ENDED 그룹 자동 생성
     */
    Entitlement createEntitlement(CreateEntitlementRequestDto requestDto);
}
