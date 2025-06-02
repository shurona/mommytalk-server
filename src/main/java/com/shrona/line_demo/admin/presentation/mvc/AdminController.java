package com.shrona.line_demo.admin.presentation.mvc;

import static com.shrona.line_demo.common.core.StaticVariable.HOME_VIEW;

import com.shrona.line_demo.admin.application.AdminService;
import com.shrona.line_demo.admin.domain.AdminUser;
import com.shrona.line_demo.admin.presentation.dtos.AdminAddPhoneNumberRequestBody;
import com.shrona.line_demo.admin.presentation.dtos.AdminDeleteTestUser;
import com.shrona.line_demo.admin.presentation.form.LoginForm;
import com.shrona.line_demo.admin.presentation.form.TestUserForm;
import com.shrona.line_demo.common.core.StaticVariable;
import com.shrona.line_demo.common.dto.ChannelForm;
import com.shrona.line_demo.common.session.UserSession;
import com.shrona.line_demo.line.application.ChannelService;
import com.shrona.line_demo.line.application.LineService;
import com.shrona.line_demo.line.domain.Channel;
import com.shrona.line_demo.line.domain.LineUser;
import com.shrona.line_demo.line.presentation.form.ChannelListForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@RequiredArgsConstructor
@RequestMapping("/admin")
@Controller
public class AdminController {

    // Service
    private final AdminService adminService;
    private final LineService lineService;
    private final ChannelService channelService;

    @GetMapping({"", "/"})
    public String homeView(
        @SessionAttribute(name = StaticVariable.LOGIN_USER, required = false) UserSession userSession,
        Model model
    ) {

        if (userSession == null) {
            // 세션이 없으면
            model.addAttribute("loginForm", new LoginForm("", ""));
            return "login";
        }

        // 채널 정보를 갖고 와서 model로 넘겨준다.
        List<Channel> channelList = channelService.findChannelList();
        model.addAttribute("channels", channelList.stream().map(ChannelListForm::of).toList());

        return HOME_VIEW;
    }

    @PostMapping("/v1/login")
    public String checkLogin(
        @ModelAttribute("loginForm") LoginForm form,
        BindingResult bindingResult,
        HttpServletRequest request
    ) {
        AdminUser user = adminService.findByLoginId(form.loginId());

        if (user == null) {
            bindingResult.rejectValue("loginId", "error.loginId", "없는 사용자 입니다.");
            return "login";
        }

        if (!adminService.checkPasswordCorrect(form.password(), user.getPassword())) {
            bindingResult.rejectValue("password", "error.password", "잘못된 비밀번호 입니다.");
            return "login";
        }

        // 세션 등록
        HttpSession session = request.getSession();
        UserSession userSession = new UserSession(user.getId(), user.getLoginId());
        //TODO: 로그인 시간 외부에서 주입하도록 수정
        session.setMaxInactiveInterval(StaticVariable.LOGIN_SESSION_TIME);
        session.setAttribute(StaticVariable.LOGIN_USER, userSession);

        return "redirect:/admin";
    }


    /**
     * 테스트 발송 대상 목록 조회
     */
    @GetMapping("channels/{channelId}/test/user")
    public String selectTestMessageSelectView(
        Model model,
        @PathVariable("channelId") Long channelId
    ) {
        Optional<Channel> channelInfo = channelService.findChannelById(channelId);
        // 채널정보가 없는 경우 그냥 홈으로 보낸다.
        if (channelInfo.isEmpty()) {
            return HOME_VIEW;
        }

        List<TestUserForm> allTestUser = adminService.findAllTestUser(channelInfo.get());
        model.addAttribute("testUserList", allTestUser);

        // 채널 정보 모델에 등록
        registerChannelToModel(channelInfo.get(), model);

        return "test/send-select";
    }

    /**
     * 테스트 발송 대상 등록
     */
    @PostMapping("channels/{channelId}/test/user")
    public ResponseEntity<?> registerPhoneNumber(
        @PathVariable("channelId") Long channelId,
        @RequestBody AdminAddPhoneNumberRequestBody requestBody
    ) {

        Optional<Channel> channelInfo = channelService.findChannelById(channelId);
        // 채널정보가 없는 경우 그냥 홈으로 보낸다.
        if (channelInfo.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // 휴대전화를 기준으로 라인 아이디를 갖고 온다.
        Optional<LineUser> lineUserByPhoneNumber = lineService.findLineUserByPhoneNumber(
            requestBody.phoneNumber());

        if (lineUserByPhoneNumber.isEmpty()) {
            return ResponseEntity.ok().build();
        }

        adminService.registerTestNumber(channelInfo.get(), lineUserByPhoneNumber.get());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("channels/{channelId}/test/user")
    public ResponseEntity<?> deleteTestUser(
        @PathVariable("channelId") Long channelId,
        @RequestBody AdminDeleteTestUser requestBody
    ) {

        Optional<Channel> channelInfo = channelService.findChannelById(channelId);
        // 채널정보가 없는 경우 그냥 홈으로 보낸다.
        if (channelInfo.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        adminService.deleteTestUser(channelInfo.get(), requestBody.id());

        return ResponseEntity.ok().build();
    }

    private void registerChannelToModel(Channel channel, Model model) {
        model.addAttribute("channelInfo", ChannelForm.of(channel));
    }

}
