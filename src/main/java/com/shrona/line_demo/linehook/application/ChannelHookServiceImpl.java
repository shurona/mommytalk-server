package com.shrona.line_demo.linehook.application;

import static com.shrona.line_demo.user.domain.vo.PhoneNumber.changeWithoutError;

import com.shrona.line_demo.common.utils.PhoneProcess;
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

        Optional<User> userInfo = userRepository.findByLineUser(lineUserInfo);

        // 이미 User에 라인 유저가 존재하거나 휴대전화가 존재하면 진행하지 않는다.
        if (userInfo.isPresent() && userInfo.get().getPhoneNumber() != null) {
            String phone = userInfo.get().getPhoneNumber().getPhoneNumber();
            if (phone != null && !phone.isBlank()) {
                return false;
            }
        }

        // 휴대전화 번호 형식인지 확인 후 로직 처리
        return validatePhoneAndMatchUser(content.trim(), channelLineUser);
    }

    /**
     * 라인에 휴대전화 번호 등록 시 휴대전화 번호가 맞고 LineUser가 없으면 매칭해준다.
     */
    @Transactional
    public boolean validatePhoneAndMatchUser(String content, ChannelLineUser channelLineUser) {
        // content가 휴대전화가 format이 아니면 false 리턴
        if (!phoneProcess.isValidFormat(content)) {
            return false;
        }

        PhoneNumber phoneNumber = changeWithoutError(content);
        LineUser lineUser = channelLineUser.getLineUser();

        // LineUser 연결 상태 확인 및 처리
        Optional<User> existingUserByLineUser = userRepository.findByLineUser(lineUser);
        if (existingUserByLineUser.isPresent()) {
            return handleExistingLineUserConnection(existingUserByLineUser.get(), phoneNumber);
        }

        // 휴대전화로 기존 User 조회 및 연결
        Optional<User> existingUserByPhone = userRepository.findByPhoneNumber(phoneNumber);
        if (existingUserByPhone.isPresent()) {
            return connectLineUserToExistingUser(existingUserByPhone.get(), lineUser, phoneNumber);
        }

        // 신규 User 생성
        createNewUserWithLineConnection(phoneNumber, lineUser);
        return true;
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

        messageUtils.registerSingleTask(
            channel.get(),
            lineUser.get(),
            channel.get().getInviteMessage(),
            LocalDateTime.now());
    }

    /**
     * LineUser가 이미 연결된 경우 처리
     * - 휴대전화가 존재하면 업데이트하지 않음 (false)
     * - 휴대전화가 비어있으면 업데이트 (true)
     */
    private boolean handleExistingLineUserConnection(User existingUser, PhoneNumber phoneNumber) {
        if (existingUser.hasPhoneNumber()) {
            // 이미 휴대전화가 있으면 업데이트하지 않음
            return false;
        }

        // 휴대전화가 없으면 업데이트
        existingUser.updatePhoneNumber(phoneNumber);
        return true;
    }

    /**
     * 기존 User에 LineUser 연결 시도
     */
    private boolean connectLineUserToExistingUser(User existingUser, LineUser lineUser,
        PhoneNumber phoneNumber) {
        // 이미 다른 LineUser와 연결되어 있다면 매칭하지 않음
        if (existingUser.hasLineUser()) {
            return false;
        }

        // 기존 User에 LineUser 연결
        existingUser.updateLineAndPhoneNumber(lineUser, phoneNumber);
        return true;
    }

    /**
     * 신규 User 생성 및 LineUser 연결
     */
    private void createNewUserWithLineConnection(PhoneNumber phoneNumber, LineUser lineUser) {
        User newUser = User.createUserWithLine(phoneNumber, lineUser);
        userRepository.save(newUser);
    }
}
