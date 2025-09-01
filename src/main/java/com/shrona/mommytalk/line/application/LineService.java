package com.shrona.mommytalk.line.application;

import com.shrona.mommytalk.line.domain.Channel;
import com.shrona.mommytalk.line.domain.ChannelUserConnection;
import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.line.infrastructure.dao.ChannelUserConnectionWithPhoneDao;
import com.shrona.mommytalk.user.domain.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LineService {

    /**
     * 라인 아이디를 기준으로 라인 유저 조회
     */
    public Optional<LineUser> findLineUserByLineId(String lineId);

    /**
     * 휴대전화를 기준으로 라인 유저 조회
     */
    public Optional<LineUser> findLineUserByPhoneNumber(String phoneNumber);

    /**
     * 채널 유저 연결 목록 조회
     */
    public Page<ChannelUserConnectionWithPhoneDao> findChannelUserConnectionListByChannel(
        Channel channel, Pageable pageable);

    /**
     * 채널 유저 연결 목록 조건 조회
     */
    public Page<ChannelUserConnectionWithPhoneDao> findChannelUserConnectionListByChannelAndQuery(
        Channel channel, String Query, Pageable pageable);

    /**
     * ChannelUserConnection을 조회하고 없으면 생성
     */
    public ChannelUserConnection findOrCreateChannelUserConnection(Channel channel, User user);

    /**
     * 라인 유저를 조회하고 없으면 생성해준다.
     */
    public LineUser findOrCreateLineUser(String lineId);

    /**
     * 라인 유저 아이디를 기준으로 휴대전화를 수정한다.
     */
    public LineUser updateLineUserPhoneNumber(Long id, String phoneNumber);

    /**
     * 유저와 채널 아이디를 기준으로 팔로우 해준다.
     */
    public ChannelUserConnection followChannelAndUser(Channel channel, User user);

    /**
     * 유저와 채널 아이디를 기준으로 언팔로우 해준다.
     */
    public void unfollowChannelAndUser(Channel channel, User user);

    /**
     * 라인 유저의 휴대전화를 초기화
     */
    public void clearLineUserPhoneNumber(String lineId);


}
