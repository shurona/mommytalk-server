package com.shrona.line_demo.user.presentation.controller;

import static com.shrona.line_demo.common.core.StaticVariable.HOME_VIEW;

import com.shrona.line_demo.common.dto.ChannelForm;
import com.shrona.line_demo.common.dto.PagingForm;
import com.shrona.line_demo.line.application.ChannelService;
import com.shrona.line_demo.line.domain.Channel;
import com.shrona.line_demo.user.application.GroupService;
import com.shrona.line_demo.user.domain.Group;
import com.shrona.line_demo.user.presentation.form.BuyerForm;
import com.shrona.line_demo.user.presentation.form.GroupAddUserRequestBody;
import com.shrona.line_demo.user.presentation.form.GroupCreateRequestBody;
import com.shrona.line_demo.user.presentation.form.GroupDeleteRequestBody;
import com.shrona.line_demo.user.presentation.form.GroupDeleteUserRequestBody;
import com.shrona.line_demo.user.presentation.form.GroupForm;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@RequestMapping("/admin/channels/{channelId}/groups")
@Controller
public class GroupController {

    private final GroupService groupService;
    private final ChannelService channelService;

    /**
     * 그룹 목록 조회
     */
    @GetMapping("/list")
    public String groupListView(
        @PathVariable("channelId") Long channelId,
        @RequestParam(value = "page", defaultValue = "0") int pageNumber,
        Model model
    ) {
        String groupListUrl = "/admin/channels/" + channelId + "/groups/list";

        Optional<Channel> channelInfo = channelService.findChannelById(channelId);
        // 채널정보가 없는 경우 그냥 홈으로 보낸다.
        if (channelInfo.isEmpty()) {
            return HOME_VIEW;
        }

        Page<Group> groupWithPage = groupService.findGroupList(
            channelInfo.get(), PageRequest.of(pageNumber, 20));
        List<GroupForm> groupList = groupWithPage.stream().map(GroupForm::of).toList();

        // 페이징 추가
        model.addAttribute("pagingInfo",
            PagingForm.of(
                groupWithPage.getNumber(), groupWithPage.getTotalPages(),
                groupListUrl));

        // group 추가
        model.addAttribute("groups", groupList);

        // 채널 정보 모델에 등록
        registerChannelToModel(channelInfo.get(), model);

        return "group/list";
    }

    /**
     * 새 그룹 추가
     */
    @PostMapping
    public ResponseEntity<?> createNewGroup(
        @PathVariable("channelId") Long channelId,
        @Validated @RequestBody GroupCreateRequestBody requestBody
    ) {
        Optional<Channel> channelInfo = channelService.findChannelById(channelId);
        // 채널정보가 없는 경우 그냥 홈으로 보낸다.
        if (channelInfo.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        groupService.createGroup(
            channelInfo.get(), requestBody.name(), requestBody.description(),
            requestBody.phoneNumberList());

        return ResponseEntity.ok().build();
    }

    /**
     * 구매자 그룹 상세조회 페이지 view
     */
    @GetMapping("/{id}")
    public String groupDetailView(
        @PathVariable("channelId") Long channelId,
        @PathVariable("id") Long id,
        Model model
    ) {
        Optional<Channel> channelInfo = channelService.findChannelById(channelId);
        // 채널정보가 없는 경우 그냥 홈으로 보낸다.
        if (channelInfo.isEmpty()) {
            return HOME_VIEW;
        }

        Group groupInfo = groupService.findGroupById(id, true);

        // null이면 목록으로 반환
        if (groupInfo == null) {
            return "group/list";
        }

        // groupInfo
        model.addAttribute("group", GroupForm.of(groupInfo));

        // phone detail Info(휴대전화가 있는 유저만 전달)
        model.addAttribute("buyers",
            groupInfo.getUserGroupList().stream()
                .filter(gu -> gu.getUser().getPhoneNumber() != null).map(BuyerForm::of).toList());

        // 채널 정보 모델에 등록
        registerChannelToModel(channelInfo.get(), model);

        return "group/details";
    }

    @PostMapping("/{id}/users")
    public ResponseEntity<?> addUserToGroup(
        @PathVariable("id") Long groupId,
        @RequestBody GroupAddUserRequestBody requestBody
    ) {
        // 유저 추가
        groupService.addUserToGroup(groupId, requestBody.phoneNumberList());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<?> deleteGroupIds(
        @RequestBody GroupDeleteRequestBody requestBody
    ) {
        groupService.softDeleteGroup(requestBody.groupIds());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/users")
    public ResponseEntity<?> deleteUserToGroup(
        @PathVariable("id") Long groupId,
        @RequestBody GroupDeleteUserRequestBody requestBody
    ) {

        groupService.deleteUserFromGroupByIds(groupId, requestBody.userGroupIds());

        return ResponseEntity.ok().build();
    }

    private void registerChannelToModel(Channel channel, Model model) {
        model.addAttribute("channelInfo", ChannelForm.of(channel));
    }

}
