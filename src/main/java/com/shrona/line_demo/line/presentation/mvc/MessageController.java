package com.shrona.line_demo.line.presentation.mvc;

import static com.shrona.line_demo.common.utils.StaticVariable.HOME_VIEW;
import static com.shrona.line_demo.line.presentation.form.TargetType.ALL;
import static com.shrona.line_demo.line.presentation.form.TargetType.GROUP;

import com.shrona.line_demo.common.dto.ChannelForm;
import com.shrona.line_demo.common.dto.PagingForm;
import com.shrona.line_demo.line.application.ChannelService;
import com.shrona.line_demo.line.application.MessageService;
import com.shrona.line_demo.line.application.sender.MessageSender;
import com.shrona.line_demo.line.domain.Channel;
import com.shrona.line_demo.line.domain.MessageLog;
import com.shrona.line_demo.line.presentation.dtos.MessageLogUpdateRequestDto;
import com.shrona.line_demo.line.presentation.form.MessageListForm;
import com.shrona.line_demo.line.presentation.form.MessageSendForm;
import com.shrona.line_demo.line.presentation.form.MessageTestForm;
import com.shrona.line_demo.line.presentation.form.TargetType;
import com.shrona.line_demo.user.application.GroupService;
import com.shrona.line_demo.user.domain.Group;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@RequestMapping("/admin/channels/{channelId}/messages")
@Controller
public class MessageController {

    // service
    private final MessageService messageService;
    private final GroupService groupService;
    private final ChannelService channelService;

    // sender
    private final MessageSender messageSender;

    @GetMapping
    public String sendMessageView(
        @PathVariable("channelId") Long channelId,
        Model model) {

        Optional<Channel> channelInfo = channelService.findChannelById(channelId);
        // 채널정보가 없는 경우 그냥 홈으로 보낸다.
        if (channelInfo.isEmpty()) {
            return HOME_VIEW;
        }

        // 채널 정보 모델에 등록
        registerChannelToModel(channelInfo.get(), model);

        // 그룹 목록 조회 및 이름 추출
        registerGroupModel(channelInfo.get(), model);

        model.addAttribute("messageForm",
            MessageSendForm.of("", LocalDateTime.now(),
                new ArrayList<>(), new ArrayList<>(), GROUP));

        return "message/send";
    }

    /**
     * 메시지 목록 조회
     */
    @GetMapping("/list")
    public String messageListView(
        @PathVariable("channelId") Long channelId,
        @RequestParam(value = "page", defaultValue = "0") int pageNumber,
        Model model
    ) {

        Optional<Channel> channelInfo = channelService.findChannelById(channelId);
        // 채널정보가 없는 경우 그냥 홈으로 보낸다.
        if (channelInfo.isEmpty()) {
            return HOME_VIEW;
        }

        // 정렬을 포함해서 Pageable을 전달해서 메시지 목록을 받는다.
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));
        Page<MessageLog> messageLogList = messageService.findMessageLogList(channelInfo.get(),
            PageRequest.of(pageNumber, 15, sort));

        // groupId : 유저 갯수 Map을 조회한다.
        Map<Long, Integer> logLineIdCount = messageService.findLineIdCountByLog(
            messageLogList.stream().map(MessageLog::getId).toList()
        );

        // 페이징 정보
        model.addAttribute("pagingInfo",
            PagingForm.of(
                messageLogList.getNumber(), messageLogList.getTotalPages()));

        // 메시지 목록 정보
        model.addAttribute("messages",
            messageLogList.map(m -> MessageListForm.of(m, logLineIdCount)).toList());

        // 채널 정보 모델에 등록
        registerChannelToModel(channelInfo.get(), model);

        return "message/list";
    }

    /**
     * 보낼 메시지를 저장하는 Controller
     */
    @PostMapping("/v1/send")
    public String sendMessage(
        @PathVariable("channelId") Long channelId, // 채널 아이디
        Model model,
        @ModelAttribute("messageForm") MessageSendForm form,
        BindingResult bindingResult
    ) {
        // 시간 정보를 로컬 시간 정보(utc)로 변환해준다.
        ZonedDateTime serverDateTime = form.sendDateTimeUtc()
            .withZoneSameInstant(ZoneId.systemDefault());
        LocalDateTime localDateTime = serverDateTime.toLocalDateTime();

        Optional<Channel> channelInfo = channelService.findChannelById(channelId);
        // 채널정보가 없는 경우 그냥 홈으로 보낸다.
        if (channelInfo.isEmpty()) {
            return HOME_VIEW;
        }

        // 전송이 특정 그룹인 경우
        if (form.targetType().equals(GROUP.getType())) {
            // 그룹 타겟 전송인데 그룹이 비어있는 경우
            if (form.includeGroup() == null || form.includeGroup().isEmpty()) {
                return groupFillAndReturn(model, form, bindingResult, channelInfo);
            }
            messageService.createMessageSelectGroup(
                channelInfo.get(),
                1L, form.includeGroup(), form.excludeGroup(),
                localDateTime,
                form.content());
        }
        // 전송이 전체 인 경우
        else if (form.targetType().equals(ALL.getType())) {
            messageService.createMessageAllGroup(
                channelInfo.get(), 1L, form.excludeGroup(), localDateTime, form.content());
        }

        return "redirect:/admin/channels/" + channelId + "/messages/list";
    }

    /**
     * 메시지를 취소 하는 함수
     */
    @PatchMapping("/{messageId}/cancel")
    public ResponseEntity<?> cancelMessageLog(
        @PathVariable("channelId") Long channelId, // 채널 아이디
        @PathVariable("messageId") Long messageId // 메시지 아이디
    ) {

        messageService.cancelSendMessage(messageId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{messageId}")
    public ResponseEntity<?> updateMessageLog(
        @PathVariable("channelId") Long channelId, // 채널 아이디
        @PathVariable("messageId") Long messageId, // 메시지 아이디
        @RequestBody MessageLogUpdateRequestDto requestDto
    ) {

        messageService.updateMessageLog(messageId, requestDto.content());

        return ResponseEntity.ok().build();

    }


    /**
     * 테스트 전송 요청
     */
    @PostMapping("/v1/send/test")
    public ResponseEntity<?> testDeliver(
        @PathVariable("channelId") Long channelId,
        @RequestBody MessageTestForm form
    ) {
        Optional<Channel> channelInfo = channelService.findChannelById(channelId);
        // 채널정보가 없는 경우 그냥 홈으로 보낸다.
        if (channelInfo.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (messageSender.sendTestLineMessage(channelInfo.get(), form.content())) {
            return ResponseEntity.ok(Map.of("success", true));
        } else {
            return ResponseEntity.internalServerError().build();
        }


    }

    /**
     * 그룹이 비어 있으면 다시 전달해준다.
     */
    private String groupFillAndReturn(Model model, MessageSendForm form,
        BindingResult bindingResult,
        Optional<Channel> channelInfo) {
        bindingResult.rejectValue("includeGroup", "error.non-group", "포함할 친구 그룹을 선택하세요.");

        // group 목록 추가
        registerGroupModel(channelInfo.get(), model);
        // 채널 정보 모델에 등록
        registerChannelToModel(channelInfo.get(), model);

        // model의 기본 데이터를 초기화 해주고 binding result 새로 매핑해준다.
        model.addAttribute("messageForm", initMessageSendFormByForm(form));
        model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "messageForm", bindingResult);

        return "message/send";
    }

    /**
     * Group model의 등록(메시지 보내기 화면에서 그룹 목록 보여주기)
     */
    private void registerGroupModel(Channel channel, Model model) {
        Page<Group> groupList = groupService.findGroupList(channel, PageRequest.of(0, 1000));

        Map<Long, String> groupNameList = groupList.stream()
            .collect(Collectors.toMap(Group::getId, Group::getName));

        // 글로벌로 처리할 Form
        model.addAttribute("groupForm", groupNameList);
    }

    /**
     * 에러 발생 시 MessageSendForm을 다시 만들어줌.
     */
    private MessageSendForm initMessageSendFormByForm(MessageSendForm form) {
        return MessageSendForm.of(form.content(), form.sendDateTime(), new ArrayList<>(),
            new ArrayList<>(), TargetType.valueOf(form.targetType()));
    }

    private void registerChannelToModel(Channel channel, Model model) {
        model.addAttribute("channelInfo", ChannelForm.of(channel));
    }

}
