package com.shrona.mommytalk.admin.application;

import com.shrona.mommytalk.admin.domain.AdminUser;
import com.shrona.mommytalk.admin.domain.TestUser;
import com.shrona.mommytalk.admin.infrastructure.AdminJpaRepository;
import com.shrona.mommytalk.admin.infrastructure.TestUserJpaRepository;
import com.shrona.mommytalk.admin.presentation.form.TestUserForm;
import com.shrona.mommytalk.line.domain.Channel;
import com.shrona.mommytalk.user.common.exception.UserErrorCode;
import com.shrona.mommytalk.user.common.exception.UserException;
import com.shrona.mommytalk.user.domain.User;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AdminServiceImpl implements AdminService {

    // repository
    private final AdminJpaRepository adminRepository;
    private final TestUserJpaRepository testUserRepository;

    // passwordEncoder
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<AdminUser> findAdminUserList() {

        return adminRepository.findAll();
    }

    @Override
    public AdminUser findById(Long id) {
        return adminRepository.findById(id).orElseThrow(
            () -> new UserException(UserErrorCode.USER_NOT_FOUND)
        );
    }

    @Override
    public AdminUser findByLoginId(String loginId) {

        return adminRepository.findByLoginId(loginId).orElse(null);
    }

    public boolean checkPasswordCorrect(String inputPassword, String dbPassword) {
        return passwordEncoder.matches(inputPassword, dbPassword);
    }

    @Transactional
    public AdminUser createAdminUser(String loginId, String password, String lineId) {

        Optional<AdminUser> userInfo = adminRepository.findByLoginId(loginId);

        if (userInfo.isPresent()) {
            return null;
        }
        return adminRepository.save(
            AdminUser.createAdminUser(loginId, passwordEncoder.encode(password), lineId));
    }

    @Override
    public List<TestUserForm> findAllTestUser(Channel channel) {
        return testUserRepository.findAllByChannel(channel).stream()
            .map(TestUserForm::of).toList();
    }

    @Transactional
    public void registerTestNumber(Channel channel, User userInfo) {
        Optional<TestUser> testUser = testUserRepository
            .findByChannelAndUser(channel, userInfo);
        if (testUser.isEmpty()) {
            testUserRepository.save(TestUser.createTestUser(channel, userInfo, "설명"));
        }
    }

    @Transactional
    public void deleteTestUser(Channel channel, Long id) {
        Optional<TestUser> byId = testUserRepository.findById(id);
        if (byId.isEmpty()) {
            return;
        }
        testUserRepository.deleteById(id);
    }
}
