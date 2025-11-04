package com.shrona.mommytalk.openai.application;

import com.shrona.mommytalk.message.domain.MessageType;
import com.shrona.mommytalk.openai.domain.MessagePrompt;
import com.shrona.mommytalk.openai.domain.UserSentenceHistory;

public interface OpenAiService {

    String testPrompt();

    /**
     * 어드민에서 데이터를 생성한다.
     */
    String generateData(
        MessagePrompt prompt, MessageType type, int userLevel, int childLevel);


    /**
     * 유저가 데이터를 생성한다.
     */
    String generateDataByUser(MessagePrompt prompt, UserSentenceHistory userSentenceHistory);
}
