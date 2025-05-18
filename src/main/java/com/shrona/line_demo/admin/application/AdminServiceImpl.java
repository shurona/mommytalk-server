package com.shrona.line_demo.admin.application;

import com.shrona.line_demo.admin.domain.AdminUser;
import com.shrona.line_demo.admin.infrastructure.AdminJpaRepository;
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

    private final AdminJpaRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<AdminUser> findAdminUserList() {

        return adminRepository.findAll();
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
}
