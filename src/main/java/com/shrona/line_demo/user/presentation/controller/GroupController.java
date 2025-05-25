package com.shrona.line_demo.user.presentation.controller;

import com.shrona.line_demo.common.dto.PagingForm;
import com.shrona.line_demo.user.application.GroupService;
import com.shrona.line_demo.user.domain.Group;
import com.shrona.line_demo.user.presentation.form.BuyerForm;
import com.shrona.line_demo.user.presentation.form.GroupAddUserRequestBody;
import com.shrona.line_demo.user.presentation.form.GroupCreateRequestBody;
import com.shrona.line_demo.user.presentation.form.GroupDeleteRequestBody;
import com.shrona.line_demo.user.presentation.form.GroupDeleteUserRequestBody;
import com.shrona.line_demo.user.presentation.form.GroupForm;
import java.util.List;
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
@RequestMapping("/admin/groups")
@Controller
public class GroupController {

    private final GroupService groupService;

    @GetMapping("/list")
    public String groupListView(
        @RequestParam(value = "page", defaultValue = "0") int pageNumber,
        Model model
    ) {

        String groupListUrl = "/admin/groups/list";

        Page<Group> groupWithPage = groupService.findGroupList(PageRequest.of(pageNumber, 20));
        List<GroupForm> groupList = groupWithPage.stream().map(GroupForm::of).toList();

        // 페이징 추가
        model.addAttribute("pagingInfo",
            PagingForm.of(
                groupWithPage.getNumber(), groupWithPage.getTotalPages(),
                groupListUrl));

        // group 추가
        model.addAttribute("groups", groupList);

        return "group/list";
    }

    @PostMapping
    public String createNewGroup(
        @Validated @RequestBody GroupCreateRequestBody requestBody
    ) {

        groupService.createGroup(requestBody.name(), requestBody.description(),
            requestBody.phoneNumberList());

        return "redirect:/admin/groups/list";
    }

    @GetMapping("/{id}")
    public String groupDetailView(
        @PathVariable("id") Long id,
        Model model
    ) {

        Group groupInfo = groupService.findGroupById(id, true);

        // null이면 목록으로 반환
        if (groupInfo == null) {
            return "group/list";
        }

        // groupInfo
        model.addAttribute("group", GroupForm.of(groupInfo));

        // phone detail Info
        model.addAttribute("buyers",
            groupInfo.getUserGroupList().stream().map(BuyerForm::of).toList());

        return "group/details";
    }

    @PostMapping("/{id}/users")
    public String addUserToGroup(
        @PathVariable("id") Long groupId,
        @RequestBody GroupAddUserRequestBody requestBody
    ) {

        System.out.println("여긴가요? : " + requestBody);

        // 유저 추가
        groupService.addUserToGroup(groupId, requestBody.phoneNumberList());

        return "redirect:/admin/groups/" + groupId;
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

}
