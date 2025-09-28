package com.shrona.mommytalk.openai.application;

public interface OpenAiService {

    String testPrompt();

    String generateData(String prompt);

}
