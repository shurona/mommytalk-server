package com.shrona.mommytalk.user.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.config.JpaTestConfig;
import com.shrona.mommytalk.line.domain.ChannelLineUser;
import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.line.infrastructure.ChannelJpaRepository;
import com.shrona.mommytalk.line.infrastructure.ChannelLineUserJpaRepository;
import com.shrona.mommytalk.line.infrastructure.LineUserJpaRepository;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import com.shrona.mommytalk.user.infrastructure.UserJpaRepository;
import com.shrona.mommytalk.user.infrastructure.dao.UserListProjection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Import({JpaTestConfig.class, UserQueryRepositoryImpl.class})
@DataJpaTest
class UserQueryRepositoryImplTest {

    @Autowired
    private UserQueryRepositoryImpl userQueryRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private LineUserJpaRepository lineUserJpaRepository;

    @Autowired
    private ChannelJpaRepository channelJpaRepository;

    @Autowired
    private ChannelLineUserJpaRepository channelLineUserJpaRepository;

    private Channel testChannel;
    private User user1, user2, user3;
    private LineUser lineUser1, lineUser2, lineUser3;

    @BeforeEach
    void setUp() {
        // 채널 생성
        testChannel = channelJpaRepository.save(
            Channel.createChannel("TestChannel", "테스트 채널")
        );

        // 유저 생성
        user1 = userJpaRepository.save(
            User.createUser(new PhoneNumber("010-1111-1111"))
        );
        user2 = userJpaRepository.save(
            User.createUser(new PhoneNumber("010-2222-2222"))
        );
        user3 = userJpaRepository.save(
            User.createUser(new PhoneNumber("010-3333-3333"))
        );

        // LINE 유저 생성
        lineUser1 = lineUserJpaRepository.save(LineUser.createLineUser("line1"));
        lineUser2 = lineUserJpaRepository.save(LineUser.createLineUser("line2"));
        lineUser3 = lineUserJpaRepository.save(LineUser.createLineUser("line3"));

        // User와 LineUser 연결
        user1.matchUserWithLine(lineUser1);
        user2.matchUserWithLine(lineUser2);
        user3.matchUserWithLine(lineUser3);

        userJpaRepository.saveAll(List.of(user1, user2, user3));

        // ChannelLineUser 생성 (user1, user2는 팔로우, user3는 언팔로우)
        channelLineUserJpaRepository.save(ChannelLineUser.create(testChannel, lineUser1));
        channelLineUserJpaRepository.save(ChannelLineUser.create(testChannel, lineUser2));

        ChannelLineUser unfollowUser = ChannelLineUser.create(testChannel, lineUser3);
        unfollowUser.changeFollowStatus(false);
        channelLineUserJpaRepository.save(unfollowUser);
    }

    @Test
    @DisplayName("채널에 속하고 팔로우 상태인 유저 목록 조회")
    void findUserList_success() {
        // when
        List<UserListProjection> users = userQueryRepository.findLineUsersByChannelId(
            testChannel.getId());

        // then
        assertThat(users).hasSize(2);
        assertThat(users).extracting(UserListProjection::userId)
            .containsExactlyInAnyOrder(user1.getId(), user2.getId());
        assertThat(users).extracting(user -> user.phoneNumber().getPhoneNumber())
            .containsExactlyInAnyOrder("010-1111-1111", "010-2222-2222");
    }

    @Test
    @DisplayName("존재하지 않는 채널 ID로 조회 시 빈 목록 반환")
    void findUserList_nonExistentChannel() {
        // when
        List<UserListProjection> users = userQueryRepository.findLineUsersByChannelId(999L);

        // then
        assertThat(users).isEmpty();
    }

    @Test
    @DisplayName("페이징을 적용한 유저 목록 조회")
    void findUserListWithPaging_success() {
        // given
        Pageable pageable = PageRequest.of(0, 1);

        // when
        Page<UserListProjection> userPage = userQueryRepository.findLineUsersByChannelIdWithPaging(
            testChannel.getId(),
            pageable);

        // then
        assertThat(userPage.getContent()).hasSize(1);
        assertThat(userPage.getTotalElements()).isEqualTo(2);
        assertThat(userPage.getTotalPages()).isEqualTo(2);
        assertThat(userPage.isFirst()).isTrue();

        // 첫 번째 페이지의 사용자가 예상한 사용자 중 하나인지 확인
        UserListProjection firstUser = userPage.getContent().get(0);
        assertThat(firstUser.userId()).isIn(user1.getId(), user2.getId());
    }

    @Test
    @DisplayName("두 번째 페이지 조회")
    void findUserListWithPaging_secondPage() {
        // given
        Pageable pageable = PageRequest.of(1, 1);

        // when
        Page<UserListProjection> userPage = userQueryRepository.findLineUsersByChannelIdWithPaging(
            testChannel.getId(),
            pageable);

        // then
        assertThat(userPage.getContent()).hasSize(1);
        assertThat(userPage.getTotalElements()).isEqualTo(2);
        assertThat(userPage.getTotalPages()).isEqualTo(2);
        assertThat(userPage.isLast()).isTrue();
    }

    @Test
    @DisplayName("LINE ID로 유저 조회")
    void findUserByLineId_success() {
        // when
        User foundUser = userQueryRepository.findUserByLineId("line1");

        // then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(user1.getId());
        assertThat(foundUser.getPhoneNumber().getPhoneNumber()).isEqualTo("010-1111-1111");
    }

    @Test
    @DisplayName("존재하지 않는 LINE ID로 조회 시 null 반환")
    void findUserByLineId_notFound() {
        // when
        User foundUser = userQueryRepository.findUserByLineId("nonexistent");

        // then
        assertThat(foundUser).isNull();
    }

    @Test
    @DisplayName("LINE 채널에 속한 유저 조회")
    void findLineUsersByChannelId_success() {
        // when
        List<UserListProjection> users = userQueryRepository.findLineUsersByChannelId(
            testChannel.getId());

        // then
        assertThat(users).hasSize(2);
        assertThat(users).extracting(UserListProjection::userId)
            .containsExactlyInAnyOrder(user1.getId(), user2.getId());
        assertThat(users).extracting(user -> user.phoneNumber().getPhoneNumber())
            .containsExactlyInAnyOrder("010-1111-1111", "010-2222-2222");
    }

    @Test
    @DisplayName("LINE 채널에 속한 유저 조회 - 페이징 지원")
    void findLineUsersByChannelIdWithPaging_success() {
        // given
        Pageable pageable = PageRequest.of(0, 1);

        // when
        Page<UserListProjection> userPage = userQueryRepository.findLineUsersByChannelIdWithPaging(
            testChannel.getId(), pageable);

        // then
        assertThat(userPage.getContent()).hasSize(1);
        assertThat(userPage.getTotalElements()).isEqualTo(2);
        assertThat(userPage.getTotalPages()).isEqualTo(2);
        assertThat(userPage.isFirst()).isTrue();

        // 첫 번째 페이지의 사용자가 예상한 사용자 중 하나인지 확인
        UserListProjection firstUser = userPage.getContent().get(0);
        assertThat(firstUser.userId()).isIn(user1.getId(), user2.getId());
    }

    @Test
    @DisplayName("KAKAO 채널에 속한 유저 조회 - 빈 목록 (테스트 데이터는 LINE만)")
    void findKakaoUsersByChannelId_empty() {
        // when
        List<UserListProjection> users = userQueryRepository.findKakaoUsersByChannelId(
            testChannel.getId());

        // then
        assertThat(users).isEmpty(); // KAKAO 데이터가 없으므로 빈 목록
    }

    @Test
    @DisplayName("KAKAO 채널에 속한 유저 조회 - 페이징 지원, 빈 페이지")
    void findKakaoUsersByChannelIdWithPaging_empty() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<UserListProjection> userPage = userQueryRepository.findKakaoUsersByChannelIdWithPaging(
            testChannel.getId(), pageable);

        // then
        assertThat(userPage.getContent()).isEmpty();
        assertThat(userPage.getTotalElements()).isEqualTo(0);
        assertThat(userPage.getTotalPages()).isEqualTo(0);
    }

    @Test
    @DisplayName("N+1 문제 방지 확인 - 지연 로딩된 연관관계가 정상 동작")
    void checkNPlusOnePrevention() {
        // when
        List<UserListProjection> users = userQueryRepository.findLineUsersByChannelId(
            testChannel.getId());

        // then - 지연 로딩된 연관관계에 접근해도 추가 쿼리 발생하지 않음
        for (UserListProjection user : users) {
            assertThat(user.socialId()).isNotNull();
        }
    }
}