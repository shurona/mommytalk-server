package com.shrona.mommytalk.entitlement.presentation.controller;

import com.shrona.mommytalk.common.dto.ApiResponse;
import com.shrona.mommytalk.entitlement.application.EntitlementService;
import com.shrona.mommytalk.entitlement.domain.Entitlement;
import com.shrona.mommytalk.entitlement.presentation.dtos.request.CreateEntitlementRequestDto;
import com.shrona.mommytalk.entitlement.presentation.dtos.response.EntitlementListResponseDto;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/channels/{channelId}/entitlements")
@RestController
public class EntitlementRestController {

    private final EntitlementService entitlementService;

    /**
     * 상품 생성 및 모든 채널에 AUTO_ACTIVE, AUTO_ENDED 그룹 자동 생성
     */
    @PostMapping
    public ApiResponse<Long> createEntitlement(
        @PathVariable("channelId") Long channelId,
        @Valid @RequestBody CreateEntitlementRequestDto requestDto
    ) {
        log.info("[상품 생성 요청] name={}, type={}", requestDto.name(), requestDto.type());

        Entitlement entitlement = entitlementService.createEntitlement(requestDto);

        return ApiResponse.success(entitlement.getId());
    }

    /**
     * 상품권 목록 조회
     */
    @GetMapping
    public ApiResponse<List<EntitlementListResponseDto>> findEntitlementList(
        @PathVariable("channelId") Long channelId
    ) {
        
        List<Entitlement> entitlements = entitlementService.findAllEntitlements();

        List<EntitlementListResponseDto> result = entitlements.stream()
            .map(EntitlementListResponseDto::from)
            .toList();

        return ApiResponse.success(result);
    }

    @GetMapping("/{entitlementId}")
    public ApiResponse<?> findSingleEntitlementList(
        @PathVariable("channelId") Long channelId
    ) {

        return ApiResponse.success(null);
    }


    @DeleteMapping
    public void delEntitlement() {

    }

}
