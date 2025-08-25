package com.shrona.mommytalk.line.infrastructure;

import com.shrona.mommytalk.line.domain.Channel;
import com.shrona.mommytalk.line.domain.ChannelLineUser;
import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.line.infrastructure.dao.ChannelLineUserWithPhoneDao;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import com.shrona.mommytalk.user.infrastructure.UserJpaRepository;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
class ChannelLineUserJpaRepositoryTest {

    Channel channel;
    @Autowired
    private ChannelLineUserJpaRepository channelLineUserRepository;
    @Autowired
    private ChannelJpaRepository channelRepository;
    @Autowired
    private LineUserJpaRepository lineUserJpaRepository;

    // user 정보 저장
    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private ChannelLineUserJpaRepository channelLineUserJpaRepository;


    private List<LineUser> lineUserList;

    @BeforeEach
    public void beforeEach() {

        LineUser line1 = lineUserJpaRepository.save(LineUser.createLineUser("line1"));
        LineUser line2 = lineUserJpaRepository.save(LineUser.createLineUser("line2"));
        LineUser line3 = lineUserJpaRepository.save(LineUser.createLineUser("line3"));
        LineUser line4 = lineUserJpaRepository.save(LineUser.createLineUser("line4"));
        LineUser line5 = lineUserJpaRepository.save(LineUser.createLineUser("line5"));

        channel = channelRepository.save(Channel.createChannel("이름", "설명"));

        userJpaRepository.saveAll(createUserForTesting(
            List.of(line1, line2, line3, line4, line5)));

        lineUserList = lineUserJpaRepository.saveAll(
            List.of(line1, line2, line3, line4, line5));

        ChannelLineUser channelLineUser = ChannelLineUser.create(channel, line5);
        ChannelLineUser channelLineUser1 = ChannelLineUser.create(channel, line4);
        ChannelLineUser channelLineUser2 = ChannelLineUser.create(channel, line3);
        ChannelLineUser channelLineUser3 = ChannelLineUser.create(channel, line2);
        ChannelLineUser channelLineUser4 = ChannelLineUser.create(channel, line1);

        channelLineUserJpaRepository.saveAll(
            List.of(channelLineUser,
                channelLineUser1,
                channelLineUser2,
                channelLineUser3,
                channelLineUser4));
    }

    @Test
    public void 휴대전화_검색_테스트() {
        // given

        // when
        Page<ChannelLineUserWithPhoneDao> expect1 = channelLineUserRepository.findAllByChannelAndPhoneNumberWithUser(
            channel, "1238", PageRequest.of(0, 100));
        Page<ChannelLineUserWithPhoneDao> expect2 = channelLineUserRepository.findAllByChannelAndPhoneNumberWithUser(
            channel, "1232", PageRequest.of(0, 100));
        Page<ChannelLineUserWithPhoneDao> expect4 = channelLineUserRepository.findAllByChannelAndPhoneNumberWithUser(
            channel, "1234", PageRequest.of(0, 100));
        Page<ChannelLineUserWithPhoneDao> expectDashOne = channelLineUserRepository.findAllByChannelAndPhoneNumberWithUser(
            channel, "32-12", PageRequest.of(0, 100));
        Page<ChannelLineUserWithPhoneDao> expectDashFour = channelLineUserRepository.findAllByChannelAndPhoneNumberWithUser(
            channel, "34-12", PageRequest.of(0, 100));

        // then
        Assertions.assertThat(expect1.toList().size()).isEqualTo(1);
        Assertions.assertThat(expect4.toList().size()).isEqualTo(4);
        Assertions.assertThat(expect2.toList().size()).isEqualTo(2);
        Assertions.assertThat(expectDashOne.toList().size()).isEqualTo(1);
        Assertions.assertThat(expectDashFour.toList().size()).isEqualTo(4);

    }

    private List<User> createUserForTesting(List<LineUser> lineUserList) {
        // 테스트 시나리오에 맞는 전화번호 패턴
        List<String> phonePatterns = List.of(
            "010-1234-1232",  // "1232" 검색에 매칭 (2개 결과)
            "010-1234-1235",  // "1234" 검색에 매칭 (4개 결과)
            "010-1234-1236",  // "1234" 검색에 매칭
            "010-1234-1237",  // "1234" 검색에 매칭
            "010-1232-1238"   // "1232", "1238" 검색에 매칭
        );

        List<User> users = new ArrayList<>();
        for (int i = 0; i < lineUserList.size() && i < phonePatterns.size(); i++) {
            PhoneNumber phoneNumber = PhoneNumber.changeWithoutError(phonePatterns.get(i));
            User user = User.createUserWithLine(phoneNumber, lineUserList.get(i));
            users.add(user);
        }

        return users;
    }


}