package com.shrona.mommytalk.channel.presentation.controller;

import com.shrona.mommytalk.channel.application.ChannelService;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.channel.presentation.dtos.ChannelResponseDto;
import com.shrona.mommytalk.common.dto.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/channels")
@RestController
public class ChannelController {

    private final ChannelService channelService;

    @GetMapping
    public ApiResponse<List<ChannelResponseDto>> findChannelList() {

        List<Channel> channelList = channelService.findChannelList();

        return ApiResponse.success(
            channelList.stream().map(
                ChannelResponseDto::of
            ).toList()
        );
    }

}
