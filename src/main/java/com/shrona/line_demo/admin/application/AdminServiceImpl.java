package com.shrona.line_demo.admin.application;

import com.shrona.line_demo.admin.domain.AdminUser;
import com.shrona.line_demo.admin.domain.TestUser;
import com.shrona.line_demo.admin.infrastructure.AdminJpaRepository;
import com.shrona.line_demo.admin.infrastructure.TestUserJpaRepository;
import com.shrona.line_demo.admin.presentation.form.TestUserForm;
import com.shrona.line_demo.line.domain.Channel;
import com.shrona.line_demo.line.domain.LineUser;
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
    public void registerTestNumber(Channel channel, LineUser lineUser) {
        Optional<TestUser> testUser = testUserRepository
            .findByChannelAndLineUser(channel, lineUser);
        if (testUser.isEmpty()) {
            testUserRepository.save(TestUser.createTestUser(channel, lineUser, "설명"));
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
