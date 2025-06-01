package com.shrona.line_demo.line.application;

import static com.shrona.line_demo.line.common.exception.LineErrorCode.BAD_REQUEST;
import static com.shrona.line_demo.line.common.exception.LineErrorCode.DUPLICATE_PHONE_NUMBER;
import static com.shrona.line_demo.line.common.exception.LineErrorCode.INVALID_PHONE_NUMBER;
import static com.shrona.line_demo.line.common.exception.LineErrorCode.LINEUSER_NOT_FOUND;

import com.shrona.line_demo.line.common.exception.LineException;
import com.shrona.line_demo.line.domain.Channel;
import com.shrona.line_demo.line.domain.ChannelLineUser;
import com.shrona.line_demo.line.domain.LineUser;
import com.shrona.line_demo.line.infrastructure.ChannelLineUserJpaRepository;
import com.shrona.line_demo.line.infrastructure.LineUserJpaRepository;
import com.shrona.line_demo.user.domain.User;
import com.shrona.line_demo.user.domain.vo.PhoneNumber;
import com.shrona.line_demo.user.infrastructure.UserJpaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class LineServiceImpl implements LineService {

    // repository
    private final LineUserJpaRepository lineUserRepository;
    private final UserJpaRepository userRepository;
    private final ChannelLineUserJpaRepository channelLineUserRepository;


    @Override
    public Optional<LineUser> findLineUserByLineId(String lineId) {
        return lineUserRepository.findByLineId(lineId);
    }

    @Override
    public Page<ChannelLineUser> findChannelLineUserListByChannel(
        Channel channel, Pageable pageable) {
        return channelLineUserRepository.findAllByChannel(channel, pageable);
    }

    @Override
    public Page<ChannelLineUser> findChannelLineUserListByChannelAndQuery(Channel channel,
        String query, Pageable pageable) {
        return channelLineUserRepository.findAllByChannelAndPhoneNumber(channel, query, pageable);
    }

    @Transactional
    public ChannelLineUser findOrCreateChannelLineUser(Channel channel, LineUser lineUser) {
        Optional<ChannelLineUser> channelLineUser = channelLineUserRepository.findByChannelAndLineUser(
            channel, lineUser);

        return channelLineUser.orElseGet(
            () -> channelLineUserRepository.save(ChannelLineUser.create(channel, lineUser)));
    }

    @Transactional
    public LineUser findOrCreateLineUser(String lineId) {
        Optional<LineUser> lineUserByLineId = lineUserRepository.findByLineId(lineId);
        // 비어있으면 새로 만들어 만들어준다.
        if (lineUserByLineId.isEmpty()) {
            LineUser lineUser = LineUser.createLineUser(lineId);
            return lineUserRepository.save(lineUser);
        }
        return lineUserByLineId.get();
    }

    @Transactional
    public LineUser updateLineUserPhoneNumber(Long id, String phoneNumber) {
        // LineUser 조회
        LineUser lineUser = lineUserRepository.findById(id)
            .orElseThrow(() -> new LineException(LINEUSER_NOT_FOUND));

        PhoneNumber beforePhoneNumber = lineUser.getPhoneNumber();
        PhoneNumber savingPhone = PhoneNumber.changeWithoutError(phoneNumber);
        // 휴대폰 번호 형식 조회
        if (savingPhone == null) {
            throw new LineException(INVALID_PHONE_NUMBER);
        }

        // 이미 존재하는 지 조회
        if (lineUserRepository.findByPhoneNumber(savingPhone).isPresent()) {
            throw new LineException(DUPLICATE_PHONE_NUMBER);
        }

        // 라인 유저의 휴대전화를 변경한다.
        lineUser.settingPhoneNumber(savingPhone);

        // 유저의 휴대전화를 변경해준다.
        updateUserAfterLineUserPhoneChanged(lineUser, beforePhoneNumber, savingPhone);
        return lineUser;
    }

    @Transactional
    public ChannelLineUser followChannelAndLineUser(Channel channel, LineUser lineUser) {
        return channelLineUserRepository.findByChannelAndLineUser(channel, lineUser)
            .map(existing -> {
                existing.changeFollowStatus(true);
                return existing;
            })
            .orElseGet(
                () -> channelLineUserRepository.save(ChannelLineUser.create(channel, lineUser)));
    }


    @Transactional
    public void unfollowChannelAndLineUser(Channel channel, LineUser lineUser) {
        channelLineUserRepository.findByChannelAndLineUser(channel, lineUser)
            .ifPresent(channelLineUser -> channelLineUser.changeFollowStatus(false));

    }

    /**
     * 라인 유저의 휴대폰 변경 시 User에도 반영이 되도록 적용
     */
    private void updateUserAfterLineUserPhoneChanged(
        LineUser lineUser, PhoneNumber beforePhoneNumber, PhoneNumber afterPhoneNumber) {

        Optional<User> beforeNumber = userRepository.findByPhoneNumber(beforePhoneNumber);
        Optional<User> afterNumber = userRepository.findByPhoneNumber(afterPhoneNumber);

        // 이미 존재하면 라인유저 정보를 지워준다.
        beforeNumber.ifPresent(User::clearLineUserAndPhoneNumber);

        // 현재 입력받은 번호는 라인유저의 정보를 변경해준다.
        afterNumber.ifPresent(user -> user.matchUserWithLine(lineUser));

    }


    /**
     * 라인 유저 조회 없으면 예외 처리
     */
    private LineUser findLineUserByLineIdWithException(String lineId) {
        return lineUserRepository.findByLineId(lineId).orElseThrow(
            () -> new LineException(BAD_REQUEST)
        );
    }
}
