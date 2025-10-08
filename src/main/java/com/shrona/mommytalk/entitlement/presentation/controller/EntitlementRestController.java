package com.shrona.mommytalk.entitlement.presentation.controller;

import com.shrona.mommytalk.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("/api/v1/channels/{channelId}/entitlement")
@RestController
public class EntitlementRestController {

    @PostMapping
    public void insEntitlement(
        @PathVariable("channelId") Long channelId
    ) {

    }

    @GetMapping
    public ApiResponse<?> findEntitlementList(
        @PathVariable("channelId") Long channelId
    ) {

        return ApiResponse.success(null);
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
