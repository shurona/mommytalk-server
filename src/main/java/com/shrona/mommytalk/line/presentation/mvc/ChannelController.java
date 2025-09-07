package com.shrona.mommytalk.line.presentation.mvc;


import static com.shrona.mommytalk.common.utils.StaticVariable.HOME_VIEW;

import com.shrona.mommytalk.common.dto.ChannelForm;
import com.shrona.mommytalk.line.application.ChannelService;
import com.shrona.mommytalk.line.domain.Channel;
import com.shrona.mommytalk.line.presentation.dtos.ChannelUpdateInviteMessageRequestDto;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@RequiredArgsConstructor
//@RequestMapping("/admin/channels")
//@Controller
public class ChannelController {

    private final ChannelService channelService;

    @GetMapping("/{channelId}/invite-message")
    public String channelInviteMessage(
        @PathVariable("channelId") Long channelId,
        Model model) {

        Optional<Channel> channelInfo = channelService.findChannelById(channelId);

        // 채널정보가 없는 경우 그냥 홈으로 보낸다.
        if (channelInfo.isEmpty()) {
            return HOME_VIEW;
        }

        registerChannelToModel(channelInfo.get(), model);

        model.addAttribute("inviteMessage", channelInfo.get().getInviteMessage());

        return "channel/invite-message";
    }

    @PatchMapping("/{channelId}/invite-message")
    public ResponseEntity<?> updateInviteMessage(
        @PathVariable("channelId") Long channelId,
        @RequestBody ChannelUpdateInviteMessageRequestDto requestDto
    ) {

        channelService.updateInviteMessage(channelId, requestDto.message());

        return ResponseEntity.ok().build();
    }

    private void registerChannelToModel(Channel channel, Model model) {
        model.addAttribute("channelInfo", ChannelForm.of(channel));
    }

}
