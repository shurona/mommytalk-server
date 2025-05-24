package com.shrona.line_demo.line.presentation.controller;

import com.shrona.line_demo.line.application.LineService;
import com.shrona.line_demo.line.domain.LineUser;
import com.shrona.line_demo.line.presentation.form.LineUserForm;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class LineUserController {

    private final LineService lineService;

    @GetMapping("/admin/line/friends/list")
    public String lineFriendView(
        Model model
    ) {

        Page<LineUser> lineUserList = lineService.findLineUserList(PageRequest.of(0, 1000));

        model.addAttribute("friends", lineUserList.toList()
            .stream().map(line -> LineUserForm.of(line.getLineId(), line.getPhoneNumber(),
                line.getCreatedAt())));

        return "friend/list";
    }
}
