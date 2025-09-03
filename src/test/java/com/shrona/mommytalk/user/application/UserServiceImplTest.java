package com.shrona.mommytalk.user.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shrona.mommytalk.line.application.LineServiceImpl;
import com.shrona.mommytalk.line.domain.Channel;
import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.line.infrastructure.ChannelJpaRepository;
import com.shrona.mommytalk.line.infrastructure.dao.ChannelLineUserWithPhoneDao;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import jakarta.persistence.EntityManager;
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
        List<User> userListAfterSave = userService.findOrCreateUsersWithLinesByPhoneNumbers(
            phoneNumberList);

        // then
        assertThat(userListAfterSave.size()).isEqualTo(11);
    }

}