package com.shrona.mommytalk.admin.presentation.controller;

import static com.shrona.mommytalk.user.common.exception.UserErrorCode.LOGIN_ERROR;

import com.shrona.mommytalk.admin.application.AdminService;
import com.shrona.mommytalk.admin.domain.AdminUser;
import com.shrona.mommytalk.admin.presentation.dtos.AdminLoginRequestDto;
import com.shrona.mommytalk.admin.presentation.dtos.AdminLoginResponseDto;
import com.shrona.mommytalk.admin.presentation.dtos.AdminUserResponseDto;
import com.shrona.mommytalk.common.dto.ApiResponse;
import com.shrona.mommytalk.common.utils.JwtUtils;
import com.shrona.mommytalk.user.common.exception.UserErrorCode;
import com.shrona.mommytalk.user.common.exception.UserException;
import com.shrona.mommytalk.user.domain.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/auth")
@RestController
public class AdminAuthController {

    private final AdminService adminService;
    private final JwtUtils jwtUtils;

    @GetMapping("/me")
    public ResponseEntity<AdminUserResponseDto> findMyInfo(HttpServletRequest request) {

        // LoginInterceptor에서 설정한 사용자 정보 읽기
        Long userId = (Long) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("userRole");

        if (userId == null) {
            log.error("사용자 ID를 찾을 수 없습니다. LoginInterceptor가 제대로 동작하지 않았을 수 있습니다.");
            throw new UserException(UserErrorCode.USER_NOT_FOUND);
        }

        AdminUser user = adminService.findById(userId);

        // 유저 정보가 없으면 에러
        if (user == null) {
            throw new UserException(UserErrorCode.USER_NOT_FOUND);
        }

        return ResponseEntity.ok(
            AdminUserResponseDto.of(
                user.getId(), user.getLoginId(), user.getLoginId(), UserRole.ADMIN.name())
        );

    }

    @PostMapping("/login")
    public ApiResponse<AdminLoginResponseDto> login(
        @RequestBody AdminLoginRequestDto requestDto
    ) {

        AdminUser user = adminService.findByLoginId(requestDto.username());

        // 유저 정보가 없거나 패스워드가 다르면 로그인 에러
        if (user == null || !adminService.checkPasswordCorrect(requestDto.password(),
            user.getPassword())) {
            throw new UserException(LOGIN_ERROR);
        }

        String token = jwtUtils.createToken(user.getId(), UserRole.ADMIN);

        AdminLoginResponseDto responseDto = AdminLoginResponseDto.of(
            token,
            AdminUserResponseDto.of(
                user.getId(), user.getLoginId(), user.getLoginId(), UserRole.ADMIN.name()
            )
        );

        return ApiResponse.success(responseDto);
    }

    @PostMapping("/logout")
    public void logout() {
        log.info("[로그아웃] 로그아웃 처리 되었습니다.");
    }

}
