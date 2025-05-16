package com.shrona.line_demo.user.application;

import com.shrona.line_demo.user.domain.Group;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupService {

    public Group createGroup(String name, String description, List<String> phoneList);

    public Group findGroupById(Long id);

    public Page<Group> findGroupList(Pageable pageable);

    public void addUserToGroup(Long groupId, List<String> phoneNumberList);

    public void deleteGroup(List<Long> groupIdList);
}
