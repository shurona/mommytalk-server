package com.shrona.mommytalk.line.application;

import static com.shrona.mommytalk.line.common.exception.LineErrorCode.BAD_REQUEST;
import static com.shrona.mommytalk.line.common.exception.LineErrorCode.DUPLICATE_PHONE_NUMBER;
import static com.shrona.mommytalk.line.common.exception.LineErrorCode.INVALID_PHONE_NUMBER;
import static com.shrona.mommytalk.line.common.exception.LineErrorCode.LINEUSER_NOT_FOUND;

import com.shrona.mommytalk.line.common.exception.LineException;
import com.shrona.mommytalk.line.domain.Channel;
import com.shrona.mommytalk.line.domain.ChannelLineUser;
import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.line.infrastructure.ChannelLineUserJpaRepository;
import com.shrona.mommytalk.line.infrastructure.LineUserJpaRepository;
import com.shrona.mommytalk.line.infrastructure.dao.ChannelLineUserWithPhoneDao;
import com.shrona.mommytalk.user.application.GroupService;
import com.shrona.mommytalk.user.application.UserService;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import java.util.Objects;
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
    private final ChannelLineUserJpaRepository channelLineUserRepository;

    // service
    private final UserService userService;
    private final GroupService groupService;


    @Override
    public Optional<LineUser> findLineUserByLineId(String lineId) {
        return lineUserRepository.findByLineId(lineId);
    }

    @Override
    public Optional<LineUser> findLineUserByPhoneNumber(String phoneNumber) {
        //todo: 여기 수정
//        return lineUserRepository.findByPhoneNumber(PhoneNumber.changeWithoutError(phoneNumber));
        return null;
    }

    @Override
    public Page<ChannelLineUserWithPhoneDao> findChannelLineUserListByChannel(
        Channel channel, Pageable pageable) {
        return channelLineUserRepository.findAllByChannel(channel, pageable);
    }

    @Override
    public Page<ChannelLineUserWithPhoneDao> findChannelLineUserListByChannelAndQuery(
        Channel channel,
        String query, Pageable pageable) {
        //TODO: 여기 수정
        return channelLineUserRepository.findAllByChannelAndPhoneNumberWithUser(channel, query,
            pageable);
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

        LineUser lineUser = lineUserRepository.findById(id)
            .orElseThrow(() -> new LineException(LINEUSER_NOT_FOUND));

        User checkExistPhoneNumberUser = userService.findUserByPhoneNumber(phoneNumber);
        PhoneNumber savingPhone = PhoneNumber.changeWithoutError(phoneNumber);
        // 휴대폰 번호 형식 조회
        if (savingPhone == null) {
            throw new LineException(INVALID_PHONE_NUMBER);
        }

        // 이미 휴대전화가 존재하는 경우 유저를 변경하지 않는다.
        if (Objects.nonNull(checkExistPhoneNumberUser)) {
            throw new LineException(DUPLICATE_PHONE_NUMBER);
        } else {
            // 유저의 휴대전화를 변경해준다.
            Optional<User> userByLineUser = userService.findUserByLineUser(lineUser);
            userByLineUser.ifPresent(user -> user.updatePhoneNumber(savingPhone));
        }

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

    @Transactional
    public void clearLineUserPhoneNumber(String lineId) {
//        Optional<LineUser> lineInfoOpt = lineUserRepository.findByLineId(lineId);
//        if (lineInfoOpt.isEmpty()) {
//            return;
//        }
//
//        LineUser lineUser = lineInfoOpt.get();
//        PhoneNumber phoneNumber = lineUser.getPhoneNumber();
//        if (phoneNumber == null || phoneNumber.getPhoneNumber() == null) {
//            return;
//        }
//
//        userService.deleteUserGroupAndUserInfo(phoneNumber.getPhoneNumber());
//        lineUser.clearPhoneNumber();
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
