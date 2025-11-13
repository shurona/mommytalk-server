package com.shrona.mommytalk.entitlement.application;

import com.shrona.mommytalk.entitlement.domain.UserEntitlement;
import com.shrona.mommytalk.entitlement.presentation.dtos.request.AddUserEntitlementRequestDto;
import com.shrona.mommytalk.entitlement.presentation.dtos.request.UpdateUserEntitlementRequestDto;
import com.shrona.mommytalk.entitlement.presentation.dtos.response.UserEntitlementResponseDto;
import java.util.List;

public interface UserEntitlementService {

    /**
     * 유저 상품권 추가 (그룹 자동 할당)
     */
    UserEntitlement addUserEntitlement(AddUserEntitlementRequestDto requestDto);

    /**
     * 유저 상품권 수정 (상태 변경 및 종료일 연장)
     */
    UserEntitlement updateUserEntitlement(Long userEntitlementId, UpdateUserEntitlementRequestDto requestDto);

    /**
     * 유저의 상품권 목록 조회
     */
    List<UserEntitlementResponseDto> getUserEntitlements(Long channelId, Long userId);

    /**
     * 만료된 상품권 처리 (스케줄러용)
     */
    void processExpiredEntitlements();
}
