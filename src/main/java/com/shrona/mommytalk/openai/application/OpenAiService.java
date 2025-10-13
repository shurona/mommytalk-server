package com.shrona.mommytalk.openai.application;

import com.shrona.mommytalk.message.domain.MessageType;
import com.shrona.mommytalk.openai.domain.MessagePrompt;

public interface OpenAiService {

    String testPrompt();

    String generateData(
        MessagePrompt prompt, MessageType type, int userLevel, int childLevel);

}
