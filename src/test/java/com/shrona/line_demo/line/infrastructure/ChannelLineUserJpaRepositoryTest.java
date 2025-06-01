package com.shrona.line_demo.line.infrastructure;

import com.shrona.line_demo.line.domain.Channel;
import com.shrona.line_demo.line.domain.ChannelLineUser;
import com.shrona.line_demo.line.domain.LineUser;
import com.shrona.line_demo.user.domain.vo.PhoneNumber;
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

        line1.settingPhoneNumber(PhoneNumber.changeWithoutError("010-1234-1232"));
        line2.settingPhoneNumber(PhoneNumber.changeWithoutError("010-1234-1235"));
        line3.settingPhoneNumber(PhoneNumber.changeWithoutError("010-1234-1236"));
        line4.settingPhoneNumber(PhoneNumber.changeWithoutError("010-1234-1237"));
        line5.settingPhoneNumber(PhoneNumber.changeWithoutError("010-1232-1238"));

        channel = channelRepository.save(Channel.createChannel("이름", "설명"));

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
        Page<ChannelLineUser> expect2 = channelLineUserRepository.findAllByChannelAndPhoneNumber(
            channel, "1232", PageRequest.of(0, 100));
        Page<ChannelLineUser> expect1 = channelLineUserRepository.findAllByChannelAndPhoneNumber(
            channel, "1238", PageRequest.of(0, 100));
        Page<ChannelLineUser> expect4 = channelLineUserRepository.findAllByChannelAndPhoneNumber(
            channel, "1234", PageRequest.of(0, 100));
        Page<ChannelLineUser> expectDashOne = channelLineUserRepository.findAllByChannelAndPhoneNumber(
            channel, "32-12", PageRequest.of(0, 100));
        Page<ChannelLineUser> expectDashFour = channelLineUserRepository.findAllByChannelAndPhoneNumber(
            channel, "34-12", PageRequest.of(0, 100));
        // then
        Assertions.assertThat(expect1.toList().size()).isEqualTo(1);
        Assertions.assertThat(expect4.toList().size()).isEqualTo(4);
        Assertions.assertThat(expect2.toList().size()).isEqualTo(2);
        Assertions.assertThat(expectDashOne.toList().size()).isEqualTo(1);
        Assertions.assertThat(expectDashFour.toList().size()).isEqualTo(4);

    }


}