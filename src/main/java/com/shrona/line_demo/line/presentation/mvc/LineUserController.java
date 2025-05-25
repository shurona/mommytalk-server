package com.shrona.line_demo.line.presentation.mvc;

import com.shrona.line_demo.common.dto.PagingForm;
import com.shrona.line_demo.line.application.LineService;
import com.shrona.line_demo.line.domain.LineUser;
import com.shrona.line_demo.line.presentation.form.LineUserForm;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Controller
public class LineUserController {

    private final LineService lineService;

    @GetMapping("/admin/line/friends/list")
    public String lineFriendView(
        @RequestParam(value = "page", defaultValue = "0") int pageNumber,
        Model model
    ) {
        String lineFriendListViewUrl = "/admin/line/friends/list";

        Page<LineUser> lineUserList = lineService.findLineUserList(PageRequest.of(pageNumber, 20));

        model.addAttribute("pagingInfo",
            PagingForm.of(
                lineUserList.getNumber(), lineUserList.getTotalPages(),
                lineFriendListViewUrl));

        model.addAttribute("friends", lineUserList.toList()
            .stream().map(line -> LineUserForm.of(line.getLineId(), line.getPhoneNumber(),
                line.getCreatedAt())));

        return "friend/list";
    }
}
