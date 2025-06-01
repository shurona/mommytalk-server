package com.shrona.line_demo.line.application;

import com.shrona.line_demo.line.domain.Channel;
import com.shrona.line_demo.line.domain.ChannelLineUser;
import com.shrona.line_demo.line.domain.LineUser;
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
     * 라인 유저 목록 조회
     */
    public Page<ChannelLineUser> findChannelLineUserListByChannel(
        Channel channel, Pageable pageable);

    /**
     * 라인 유저 목록 조건 조회
     */
    public Page<ChannelLineUser> findChannelLineUserListByChannelAndQuery(
        Channel channel, String Query, Pageable pageable);

    /**
     * ChannelLineUser를 조회하고 없으면 생성
     */
    public ChannelLineUser findOrCreateChannelLineUser(Channel channel, LineUser lineUser);

    /**
     * 라인 유저를 조회하고 없으면 생성해준다.
     */
    public LineUser findOrCreateLineUser(String lineId);

    /**
     * 라인 유저 아이디를 기준으로 휴대전화를 수정한다.
     */
    public LineUser updateLineUserPhoneNumber(Long id, String phoneNumber);

    /**
     * 라인 유저와 채널 아이디를 기준으로 팔로우 해준다.
     */
    public ChannelLineUser followChannelAndLineUser(Channel channel, LineUser lineUser);

    /**
     * 라인 유저와 채널 아이디를 기준으로 언팔로우 해준다.
     */
    public void unfollowChannelAndLineUser(Channel channel, LineUser lineUser);


}
