package com.shrona.mommytalk.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.line.application.LineServiceImpl;
import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.line.infrastructure.dao.ChannelLineUserWithPhoneDao;
import com.shrona.mommytalk.line.infrastructure.repository.jpa.ChannelJpaRepository;
import com.shrona.mommytalk.user.common.exception.UserException;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import jakarta.persistence.EntityManager;
import java.time.LocalTime;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class UserServiceImplTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private LineServiceImpl lineService;

    @Autowired
    private ChannelJpaRepository channelJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private List<User> userList;
    private List<String> phoneNumberList;
    private Channel channel;

    @BeforeEach
    void setUp() {

        channel = channelJpaRepository.save(
            Channel.createChannel("name", "description"));

        String number1 = "010-2345-6789";
        String number2 = "010-3456-7890";
        String number3 = "010-4567-8901";
        String number4 = "010-1234-5678";
        String number5 = "010-5678-9012";
        String number6 = "010-6789-0123";
        String number7 = "010-7890-1234";
        String number8 = "010-8901-2345";
        String number9 = "010-9012-3456";
        String number10 = "010-0123-4567";

        phoneNumberList = new ArrayList<>(List.of
            (number1, number2, number3, number4, number5, number6, number7, number8, number9,
                number10));

        LineUser line1 = lineService.findOrCreateLineUser("line1");
        LineUser line2 = lineService.findOrCreateLineUser("line2");
        LineUser line3 = lineService.findOrCreateLineUser("line3");
        LineUser line4 = lineService.findOrCreateLineUser("line4");
        LineUser line5 = lineService.findOrCreateLineUser("line5");

        // LineUser에서 User를 생성해서 followChannelAndUser 호출
        User user1 = User.createUserWithLine(null, line1);
        User user2 = User.createUserWithLine(null, line2);
        User user3 = User.createUserWithLine(null, line3);
        User user4 = User.createUserWithLine(null, line4);
        User user5 = User.createUserWithLine(null, line5);

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(user3);
        entityManager.persist(user4);
        entityManager.persist(user5);

        lineService.followChannelAndLineUser(channel, line1);
        lineService.followChannelAndLineUser(channel, line2);
        lineService.followChannelAndLineUser(channel, line3);
        lineService.followChannelAndLineUser(channel, line4);
        lineService.followChannelAndLineUser(channel, line5);

        userList = new ArrayList<>(List.of(
            User.createUser(new PhoneNumber(number1)),
            User.createUser(new PhoneNumber(number2)),
            User.createUser(new PhoneNumber(number3)),
            User.createUser(new PhoneNumber(number4)),
            User.createUser(new PhoneNumber(number5)),
            User.createUserWithLine(new PhoneNumber(number6), line1),
            User.createUserWithLine(new PhoneNumber(number7), line2),
            User.createUserWithLine(new PhoneNumber(number8), line3),
            User.createUserWithLine(new PhoneNumber(number9), line4),
            User.createUserWithLine(new PhoneNumber(number10), line5)
        ));
    }


    @DisplayName("기본 설정 테스트")
    @Test
    public void 기본_설정_테스트() {
        // 초기 테스트 확인
        assertThat(userList.size()).isEqualTo(10);

        List<ChannelLineUserWithPhoneDao> lineUserList = lineService.findChannelUserConnectionListByChannel(
                channel, PageRequest.of(0, 100))
            .stream().toList();

        assertThat(lineUserList.size()).isEqualTo(5);

    }

    @DisplayName("사용자 그룹이 기본 기능 테스트")
    @Test
    void 사용자그룹_추가_확인() {

        // given
        String correctPhone = "010-2234-8283";
        String wrongPhone = "03-399-3932";

        phoneNumberList.add(correctPhone);
        phoneNumberList.add(wrongPhone);

        // when
        List<User> userListAfterSave = userService.findOrCreateUsersByPhoneNumbers(
            phoneNumberList);

        // then
        assertThat(userListAfterSave.size()).isEqualTo(11);
    }

    @DisplayName("이미 휴대전화가 있는 라인 유저의 새로운 휴대전화 번호 변경 성공")
    @Test
    void 라인유저의_휴대전화_번호_변경_기존유저() {
        // given - beforeEach에서 line1은 이미 010-6789-0123 번호를 가진 유저와 연결됨
        String newPhoneNumber = "010-9999-9999"; // 새로운 번호

        // when
        userService.updateUserPhoneNumberByLineUser("line1", newPhoneNumber);

        // then - 사용자의 휴대전화 번호가 성공적으로 변경되었는지 확인
        User updatedUser = userService.findUserByPhoneNumber(newPhoneNumber);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getPhoneNumber().getPhoneNumber()).isEqualTo(newPhoneNumber);
    }

    @DisplayName("유저가 생성 되지 않은 라인 유저의 휴대전화 번호 변경")
    @Test
    void 라인유저의_휴대전화_번호_변경_신규유저() {
        // given
        String newLineId = "newLineUser";
        String newPhoneNumber = "010-7777-7777"; // 새로운 번호

        // 라인 유저만 생성 (User는 생성하지 않음)
        LineUser newLineUser = lineService.findOrCreateLineUser(newLineId);
        lineService.followChannelAndLineUser(channel, newLineUser);

        // when
        userService.updateUserPhoneNumberByLineUser(newLineId, newPhoneNumber);

        // then - 새로운 사용자가 생성되고 LineUser와 연결되었는지 확인
        User createdUser = userService.findUserByPhoneNumber(newPhoneNumber);
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getPhoneNumber().getPhoneNumber()).isEqualTo(newPhoneNumber);
        assertThat(createdUser.getLineUser().getLineId()).isEqualTo(newLineId);
    }

    @DisplayName("선호 시간 변경은 pending에만 저장되고 current는 불변")
    @Test
    void 선호_시간_변경시_pending에만_저장_테스트() {
        // given
        User user = createPreferredTimeTestUser("010-1111-0001");

        // when
        userService.updatePreferredSendTime(user.getId(), LocalTime.of(19, 30));

        // then
        assertThat(user.getPreferredSendTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(user.getPendingPreferredSendTime()).isEqualTo(LocalTime.of(19, 30));
    }

    @DisplayName("같은 날 재변경 시 pending은 마지막 값으로 덮어쓴다")
    @Test
    void 선호_시간_재변경시_마지막_값_저장_테스트() {
        // given
        User user = createPreferredTimeTestUser("010-1111-0002");
        userService.updatePreferredSendTime(user.getId(), LocalTime.of(19, 30));

        // when
        userService.updatePreferredSendTime(user.getId(), LocalTime.of(8, 0));

        // then
        assertThat(user.getPendingPreferredSendTime()).isEqualTo(LocalTime.of(8, 0));
    }

    @DisplayName("경계값 07:00, 20:00과 30분 단위 값은 허용")
    @Test
    void 선호_시간_경계값_허용_테스트() {
        // given
        User user = createPreferredTimeTestUser("010-1111-0003");

        // when
        userService.updatePreferredSendTime(user.getId(), LocalTime.of(7, 0));
        userService.updatePreferredSendTime(user.getId(), LocalTime.of(20, 0));

        // then
        assertThat(user.getPendingPreferredSendTime()).isEqualTo(LocalTime.of(20, 0));
    }

    @DisplayName("범위 밖이거나 30분 단위가 아니면 거부하고 값을 바꾸지 않는다")
    @Test
    void 선호_시간_검증_실패_테스트() {
        // given
        User user = createPreferredTimeTestUser("010-1111-0004");

        // when & then: 범위 밖 (07:00 이전 / 20:00 초과), 30분 단위 위반, 초 단위 입력
        assertThatThrownBy(() -> userService.updatePreferredSendTime(user.getId(), LocalTime.of(6, 30)))
            .isInstanceOf(UserException.class);
        assertThatThrownBy(() -> userService.updatePreferredSendTime(user.getId(), LocalTime.of(20, 30)))
            .isInstanceOf(UserException.class);
        assertThatThrownBy(() -> userService.updatePreferredSendTime(user.getId(), LocalTime.of(10, 15)))
            .isInstanceOf(UserException.class);
        assertThatThrownBy(() -> userService.updatePreferredSendTime(user.getId(), LocalTime.of(10, 0, 30)))
            .isInstanceOf(UserException.class);

        // then: current·pending 모두 불변
        assertThat(user.getPreferredSendTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(user.getPendingPreferredSendTime()).isNull();
    }

    @DisplayName("현재값과 같은 값을 다시 고르면 대기 중 변경이 취소된다")
    @Test
    void 현재값_재선택시_pending_취소_테스트() {
        // given: 19:30으로 변경 대기 중
        User user = createPreferredTimeTestUser("010-1111-0005");
        userService.updatePreferredSendTime(user.getId(), LocalTime.of(19, 30));

        // when: 현재값(10:00)을 다시 선택
        userService.updatePreferredSendTime(user.getId(), LocalTime.of(10, 0));

        // then: pending이 비워져 예정 표시가 사라진다
        assertThat(user.getPendingPreferredSendTime()).isNull();
        assertThat(user.getPreferredSendTime()).isEqualTo(LocalTime.of(10, 0));
    }

    private User createPreferredTimeTestUser(String phoneNumber) {
        User user = User.createUser(new PhoneNumber(phoneNumber));
        entityManager.persist(user);
        return user;
    }

}