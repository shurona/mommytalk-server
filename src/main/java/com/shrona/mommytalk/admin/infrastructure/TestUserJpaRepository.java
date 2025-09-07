package com.shrona.mommytalk.admin.infrastructure;

import com.shrona.mommytalk.admin.domain.TestUser;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestUserJpaRepository extends JpaRepository<TestUser, Long> {

    /**
     * 라인 유저 조회
     */
    Optional<TestUser> findByChannelAndUser(Channel channel, User userInfo);

    /**
     * 채널에 속한 테스트 유저 조회
     */
    List<TestUser> findAllByChannel(Channel channel);

}
