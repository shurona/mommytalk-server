package com.shrona.line_demo.admin.application;

import com.shrona.line_demo.admin.domain.AdminUser;
import java.util.List;

public interface AdminService {

    /**
     * Admin User 생성(현재 local에서만 사용)
     */
    AdminUser createAdminUser(String loginId, String password, String lineId);

    /**
     * 로그인 아이디를 기준으로 유저 조회
     */
    AdminUser findByLoginId(String loginId);

    /**
     * 비밀번호 검증
     */
    public boolean checkPasswordCorrect(String inputPassword, String dbPassword);

    /**
     * Admin User 목록 조회(현재는 메시지 테스트를 위함)
     */
    List<AdminUser> findAdminUserList();

}
