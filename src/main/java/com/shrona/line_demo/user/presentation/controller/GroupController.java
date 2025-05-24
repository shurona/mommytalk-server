package com.shrona.line_demo.user.presentation.controller;

import com.shrona.line_demo.user.application.GroupService;
import com.shrona.line_demo.user.domain.Group;
import com.shrona.line_demo.user.presentation.form.BuyerForm;
import com.shrona.line_demo.user.presentation.form.GroupCreateRequestBody;
import com.shrona.line_demo.user.presentation.form.GroupForm;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("/admin/groups")
@Controller
public class GroupController {

    private final GroupService groupService;

    @GetMapping("/list")
    public String groupListView(
        Model model
    ) {

        Page<Group> groupWithPage = groupService.findGroupList(PageRequest.of(0, 1000));
        List<GroupForm> groupList = groupWithPage.stream().map(GroupForm::of).toList();

        // group 추가
        model.addAttribute("groups", groupList);

        return "group/list";
    }

    @PostMapping
    public String createNewGroup(
        @RequestBody GroupCreateRequestBody requestBody
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

        // groupInfo
        model.addAttribute("group", GroupForm.of(groupInfo));

        // phone detail Info
        model.addAttribute("buyers",
            groupInfo.getUserGroupList().stream().map(BuyerForm::of).toList());

        return "group/details";
    }
}
