package com.shrona.mommytalk.admin.application;

import com.shrona.mommytalk.admin.domain.AdminUser;
import com.shrona.mommytalk.admin.presentation.form.TestUserForm;
import com.shrona.mommytalk.line.domain.Channel;
import com.shrona.mommytalk.user.domain.User;
import java.util.List;

public interface AdminService {

    /**
     * Admin User 생성(현재 local에서만 사용)
     */
    public AdminUser createAdminUser(String loginId, String password, String lineId);

    /**
     * 로그인 아이디를 기준으로 유저 조회
     */
    public AdminUser findByLoginId(String loginId);

    /**
     * 비밀번호 검증
     */
    public boolean checkPasswordCorrect(String inputPassword, String dbPassword);

    /**
     * Admin User 목록 조회(현재는 메시지 테스트를 위함)
     */
    public List<AdminUser> findAdminUserList();

    /**
     * 테스트 대상 목록 조회
     */
    public List<TestUserForm> findAllTestUser(Channel channel);

    /**
     * 테스트 유저를 등록한다.
     */
    public void registerTestNumber(Channel channel, User userInfo);

    /**
     * 테스트 유저 삭제
     */
    public void deleteTestUser(Channel channel, Long id);
}
