package com.shrona.line_demo.line.presentation.mvc;

import static com.shrona.line_demo.line.presentation.form.TargetType.GROUP;

import com.shrona.line_demo.line.application.MessageService;
import com.shrona.line_demo.line.presentation.form.MessageSendForm;
import com.shrona.line_demo.line.presentation.form.TargetType;
import com.shrona.line_demo.user.application.GroupService;
import com.shrona.line_demo.user.domain.Group;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("/admin/messages")
@Controller
public class MessageController {

    private final MessageService messageService;
    private final GroupService groupService;

    @GetMapping
    public String sendMessageView(Model model) {
        // 그룹 목록 조회 및 이름 추출
        registerGroupModel(model);

        model.addAttribute("messageForm",
            MessageSendForm.of("", LocalDateTime.now(),
                new ArrayList<>(), new ArrayList<>(), GROUP));

        return "message/send";
    }

    @GetMapping("/list")
    public String messageListView() {

        return "message/list";
    }

    @PostMapping("/v1/send")
    public String sendMessage(
        Model model,
        @ModelAttribute("messageForm") MessageSendForm form,
        BindingResult bindingResult
    ) {

        if (form.targetType().equals(GROUP.getType())) {
            // 그룹 타겟 전송인데 그룹이 비어있는 경우
            if (form.includeGroup() == null || form.includeGroup().isEmpty()) {
                bindingResult.rejectValue("includeGroup", "error.non-group", "포함할 친구 그룹을 선택하세요.");
                registerGroupModel(model);
                model.addAttribute("messageForm", initMessageSendFormByForm(form));
                model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "messageForm", bindingResult);

                return "message/send";
            }
            messageService.createMessage(
                1L, form.includeGroup(),
                LocalDateTime.of(form.sendDate(), LocalTime.of(form.sendHour(), form.sendMinute())),
                form.content());
        }

        return "redirect:/admin/message";
    }

    private void registerGroupModel(Model model) {
        Page<Group> groupList = groupService.findGroupList(PageRequest.of(0, 1000));

        Map<Long, String> groupNameList = groupList.stream()
            .collect(Collectors.toMap(Group::getId, Group::getName));

        // 글로벌로 처리할 Form
        model.addAttribute("groupForm", groupNameList);
    }

    private MessageSendForm initMessageSendFormByForm(MessageSendForm form) {
        return MessageSendForm.of(form.content(),
            LocalDateTime.of(form.sendDate(), LocalTime.of(form.sendHour(), form.sendMinute())),
            new ArrayList<>(), new ArrayList<>(), TargetType.valueOf(form.targetType()));
    }

}
