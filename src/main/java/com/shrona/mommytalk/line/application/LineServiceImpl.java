package com.shrona.mommytalk.line.application;

import static com.shrona.mommytalk.line.common.exception.LineErrorCode.BAD_REQUEST;

import com.shrona.mommytalk.line.common.exception.LineException;
import com.shrona.mommytalk.line.domain.Channel;
import com.shrona.mommytalk.line.domain.ChannelLineUser;
import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.line.infrastructure.ChannelLineUserJpaRepository;
import com.shrona.mommytalk.line.infrastructure.LineUserJpaRepository;
import com.shrona.mommytalk.line.infrastructure.dao.ChannelLineUserWithPhoneDao;
import com.shrona.mommytalk.line.infrastructure.repository.LineUserQueryRepository;
import com.shrona.mommytalk.user.application.GroupService;
import com.shrona.mommytalk.user.application.UserService;
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
    private final LineUserQueryRepository lineUserQueryRepository;

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
    public Page<ChannelLineUserWithPhoneDao> findChannelUserConnectionListByChannel(
        Channel channel, Pageable pageable) {
        return channelLineUserRepository.findAllByChannel(channel, pageable);
    }

    @Override
    public Page<ChannelLineUserWithPhoneDao> findChannelUserConnectionListByChannelAndQuery(
        Channel channel,
        String query, Pageable pageable) {
        return channelLineUserRepository.findAllByChannelAndPhoneNumberWithUser(channel,
            query,
            pageable);
    }

    @Transactional
    public ChannelLineUser findOrChannelLineUser(Channel channel, LineUser lineUser) {
        Optional<ChannelLineUser> channelLineUser = channelLineUserRepository.findByChannelAndLineUser(
            channel, lineUser);

        return channelLineUser.orElseGet(
            () -> channelLineUserRepository.save(
                ChannelLineUser.create(channel, lineUser)));
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
    public ChannelLineUser followChannelAndLineUser(Channel channel, LineUser lineUser) {
        return channelLineUserRepository.findByChannelAndLineUser(channel, lineUser)
            .map(existing -> {
                existing.changeFollowStatus(true);
                return existing;
            })
            .orElseGet(
                () -> channelLineUserRepository.save(
                    ChannelLineUser.create(channel, lineUser)));
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
