package com.shrona.line_demo.linehook.application;

import com.shrona.line_demo.common.core.PhoneProcess;
import com.shrona.line_demo.line.application.ChannelService;
import com.shrona.line_demo.line.application.LineService;
import com.shrona.line_demo.line.application.utils.MessageUtils;
import com.shrona.line_demo.line.domain.Channel;
import com.shrona.line_demo.line.domain.ChannelLineUser;
import com.shrona.line_demo.line.domain.LineMessage;
import com.shrona.line_demo.line.domain.LineUser;
import com.shrona.line_demo.linehook.infrastructure.LineMessageJpaRepository;
import com.shrona.line_demo.user.domain.User;
import com.shrona.line_demo.user.domain.vo.PhoneNumber;
import com.shrona.line_demo.user.infrastructure.UserJpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ChannelHookServiceImpl implements ChannelHookService {

    // service
    private final ChannelService channelService;
    private final LineService lineService;

    // repository
    private final LineMessageJpaRepository lineMessageRepository;
    private final UserJpaRepository userRepository;

    // message schedule utils
    private final MessageUtils messageUtils;


    // phone util
    private final PhoneProcess phoneProcess;

    @Transactional
    public ChannelLineUser followLineUserByLineId(Long channelId, String lineId) {
        Optional<Channel> channelById = channelService.findChannelById(channelId);
        // 채널 정보가 없으면 패스 => 잘못된 요청
        if (channelById.isEmpty()) {
            return null;
        }

        LineUser lineUserInfo = lineService.findOrCreateLineUser(lineId);

        return lineService.followChannelAndLineUser(
            channelById.get(), lineUserInfo);
    }

    @Transactional
    public void unfollowLineUserByLineId(Long channelId, String lineId) {
        Optional<Channel> channelById = channelService.findChannelById(channelId);
        // 채널 정보가 없으면 패스 => 잘못된 요청
        if (channelById.isEmpty()) {
            return;
        }

        LineUser lineUserInfo = lineService.findOrCreateLineUser(lineId);

        lineService.unfollowChannelAndLineUser(channelById.get(), lineUserInfo);
    }

    @Transactional
    public boolean saveLineMessage(Long channelId, String lineId, String content) {

        // channel 정보를 갖고 온다.
        Optional<Channel> channelById = channelService.findChannelById(channelId);
        // 채널 정보가 없으면 패스 => 잘못된 요청
        if (channelById.isEmpty()) {
            return false;
        }

        // lineId 정보를 갖고 온다. 없으면 저장해준다.
        LineUser lineUserInfo = lineService.findOrCreateLineUser(lineId);

        ChannelLineUser channelLineUser = lineService.findOrCreateChannelLineUser(
            channelById.get(), lineUserInfo);

        // 메시지 저장
        lineMessageRepository.save(LineMessage.createLineMessage(channelLineUser, content));

        // 라인 유저의 휴대전화가 존재하면 휴대전화 확인로직을 실행하지 않는다.
        if (lineUserInfo.getPhoneNumber() != null) {
            String phone = lineUserInfo.getPhoneNumber().getPhoneNumber();
            if (phone != null && !phone.isBlank()) {
                return false;
            }
        }

        // 휴대전화 번호 형식인지 확인 후 로직 처리
        return validatePhoneAndMatchUser(content, channelLineUser);
    }

    /**
     * 라인에 휴대전화 번호 등록 시 유저와 매칭시켜주는 메소드
     */
    public boolean validatePhoneAndMatchUser(String content, ChannelLineUser channelLineUser) {
        if (phoneProcess.isValidFormat(content)) {
            // 번호 중복 확인
            if (lineService.findLineUserByPhoneNumber(content).isPresent()) {
                return false;
            }

            // 번호 저장
            channelLineUser.getLineUser().settingPhoneNumber(new PhoneNumber(content));

            // 유저가 존재하는 지 확인하고 있으면 라인 아이디와 정보를 넣어준다.
            Optional<User> userInfo = userRepository.findByPhoneNumberAndLineUserIsNull(
                new PhoneNumber(content));

            // 라인 유저가 비어 있으면 매칭시켜준다.
            userInfo.ifPresent(user -> user.matchUserWithLine(channelLineUser.getLineUser()));
            return true;
        }
        return false;
    }

    public void sendLineMessageAfterSuccess(Long channelId, String lineId, String phoneNumber) {
        Optional<LineUser> lineUser = lineService.findLineUserByLineId(lineId);
        if (lineUser.isEmpty()) {
            return;
        }
        Optional<Channel> channel = channelService.findChannelById(channelId);
        // 채널 정보가 없으면 패스 => 잘못된 요청
        if (channel.isEmpty()) {
            return;
        }

        // 휴대폰 번호 재 확인
        if (!phoneProcess.isValidFormat(phoneNumber)) {
            return;
        }

        messageUtils.registerSingleTask(channel.get(),
            lineUser.get(),
            "휴대전화가 등록되었습니다. : " + phoneNumber, LocalDateTime.now());
    }
}
