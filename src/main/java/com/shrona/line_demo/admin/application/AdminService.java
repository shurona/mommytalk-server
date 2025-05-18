package com.shrona.line_demo.admin.application;

import com.shrona.line_demo.admin.domain.AdminUser;
import java.util.List;

public interface AdminService {

    AdminUser createAdminUser(String loginId, String password, String lineId);

    List<AdminUser> findAdminUserList();

}
