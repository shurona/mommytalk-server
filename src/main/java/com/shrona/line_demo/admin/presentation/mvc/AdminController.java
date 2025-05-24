package com.shrona.line_demo.admin.presentation.mvc;

import com.shrona.line_demo.admin.application.AdminService;
import com.shrona.line_demo.admin.domain.AdminUser;
import com.shrona.line_demo.admin.presentation.form.LoginForm;
import com.shrona.line_demo.common.core.StaticVariable;
import com.shrona.line_demo.common.session.UserSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@RequiredArgsConstructor
@RequestMapping("/admin")
@Controller
public class AdminController {

    private final AdminService adminService;

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

        return "home";
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


}
