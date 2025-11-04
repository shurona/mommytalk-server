package com.shrona.mommytalk.kakao.application;

import static com.shrona.mommytalk.common.utils.RandomUtils.tempPrefix;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.common.utils.RandomUtils;
import com.shrona.mommytalk.kakao.domain.ChannelKakaoUser;
import com.shrona.mommytalk.kakao.domain.KakaoUser;
import com.shrona.mommytalk.kakao.infrastructure.repository.jpa.ChannelKakaoJpaRepository;
import com.shrona.mommytalk.kakao.infrastructure.repository.jpa.KakaoUserJpaRepository;
import com.shrona.mommytalk.kakao.infrastructure.repository.query.KakaoQueryRepository;
import com.shrona.mommytalk.user.domain.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class KakaoServiceImpl implements KakaoService {

    private final KakaoQueryRepository kakaoQueryRepository;

    private final KakaoUserJpaRepository kakaoUserJpaRepository;
    private final ChannelKakaoJpaRepository channelKakaoJpaRepository;


    @Transactional
    public void addKakaoUserFromUserList(Channel channel, List<User> userList) {

        // 현재 있는 카카오 유저 목록을 갖고 온다.
        List<ChannelKakaoUser> channelKakaoUserByUserList = kakaoQueryRepository.findChannelKakaoUserByUserList(
            userList.stream().map(User::getId).toList()
        );

        Set<Long> userIdsInDB = channelKakaoUserByUserList.stream()
            .map(kakao -> kakao.getKakaoUser().getUser().getId()).collect(Collectors.toSet());

        // 현재 없는 목록을 갖고 유저 목록을 추출한다.
        List<User> userNotInKakao = userList.stream().filter(u -> !userIdsInDB.contains(u.getId()))
            .toList();

        // KakaoUser 및 KakaoChannel에 저장한다.
        List<ChannelKakaoUser> channelKakaoUsers = new ArrayList<>();
        for (User user : userNotInKakao) {
            // 만약 카카오 정보가 없다면 저정해준다.
            KakaoUser kakaoUser = user.getKakaoUser();
            if (user.getKakaoUser() == null) {
                kakaoUser = kakaoUserJpaRepository.save(
                    KakaoUser.createKakaoUser(RandomUtils.generateId(tempPrefix), user));
                // 유저의 카카오톡 정보도 업데이트
                user.updateKakaoUser(kakaoUser);
            }

            channelKakaoUsers.add(ChannelKakaoUser.create(channel, kakaoUser));
        }

        channelKakaoJpaRepository.saveAll(channelKakaoUsers);
    }

    @Transactional
    public ChannelKakaoUser upsertChannelKakaoUser(Channel channel, User user, String kakaoId) {
        List<ChannelKakaoUser> channelKakaoUsers = kakaoQueryRepository.findChannelKakaoUserByUserList(
            List.of(user.getId()));

        if (channelKakaoUsers.isEmpty()) {
            KakaoUser kakaoUser = user.getKakaoUser();
            if (kakaoUser == null) {

                kakaoUser = kakaoUserJpaRepository.save(
                    KakaoUser.createKakaoUser(kakaoId, user));
                // 유저의 카카오톡 정보도 업데이트
                user.updateKakaoUser(kakaoUser);
            }

            return channelKakaoJpaRepository.save(ChannelKakaoUser.create(channel, kakaoUser));
        }

        return channelKakaoUsers.getFirst();
    }
}
