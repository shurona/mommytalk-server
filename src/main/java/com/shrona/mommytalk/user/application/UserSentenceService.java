package com.shrona.mommytalk.user.application;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.openai.domain.UserSentenceHistory;
import java.util.List;

public interface UserSentenceService {

    /**
     * 유저가 생성한 기록 목록을 갖고 온다.
     */
    List<UserSentenceHistory> findUserSentenceHistoryList(Long userId, Integer year, Integer month);

    /**
     * 유저가 생성한 기록 목록을 갖고 온다.
     */
    UserSentenceHistory findUserSentenceDetail(Long userId, Long sentenceId);

    /**
     * 유저 문장 생성 기능
     */
    String generateUserSentence(Long userId, Channel channel, String sentence);

}
