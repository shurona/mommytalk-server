package com.shrona.mommytalk.entitlement.infrastructure.query;

import com.shrona.mommytalk.entitlement.domain.Entitlement;

public interface EntitlementQueryRepository {

    /**
     * 그룹이 어떤 상품에 속한지 확인한다.
     */
    Entitlement findEntitlementByGroupId(Long groudId);

}
