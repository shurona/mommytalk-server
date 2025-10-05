package com.shrona.mommytalk.openai.application;

import com.shrona.mommytalk.openai.infrastructure.sender.OpenAiClient;
import com.shrona.mommytalk.openai.infrastructure.sender.dto.OpenAiRequest;
import com.shrona.mommytalk.openai.infrastructure.sender.dto.OpenAiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class OpenAiServiceImpl implements OpenAiService {

    private final OpenAiClient openAiClient;

    @Value("${openai.openApiKey}")
    private String apiKey;

    @Override
    public String testPrompt() {

        String tempPrompt = "";

        try {
            // OpenAI API 요청 생성
            OpenAiRequest request = OpenAiRequest.builder()
                .model("gpt-4o")
                .maxTokens(1500)
                .temperature(0.7)
                .messages(List.of(
                    OpenAiRequest.Message.builder()
                        .role("user")
                        .content(tempPrompt)
                        .build()
                ))
                .build();

            // OpenAI API 호출
            OpenAiResponse response = openAiClient.sendChatCompletion(
                "Bearer " + apiKey,
                "application/json",
                request
            );

            // 응답에서 텍스트 추출
            if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                String content = response.getChoices().get(0).getMessage().getContent();
                log.info("OpenAI API 응답 성공. Tokens used: {}", response.getUsage().getTotalTokens());
                return content;
            } else {
                log.warn("OpenAI API 응답이 비어있습니다.");
                return "응답을 받지 못했습니다.";
            }

        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생: {}", e.getMessage(), e);
            return "오류가 발생했습니다: " + e.getMessage();
        }
    }

    @Override
    public String generateData(String prompt) {
        try {
            // OpenAI API 요청 생성
            OpenAiRequest request = OpenAiRequest.builder()
                .model("gpt-4o")
                .maxTokens(1500)
                .temperature(0.7)
                .messages(List.of(
                    OpenAiRequest.Message.builder()
                        .role("user")
                        .content(prompt)
                        .build()
                ))
                .build();

            // OpenAI API 호출
            OpenAiResponse response = openAiClient.sendChatCompletion(
                "Bearer " + apiKey,
                "application/json",
                request
            );

            // 응답에서 텍스트 추출
            if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                String content = response.getChoices().get(0).getMessage().getContent();
                log.info("OpenAI API 응답 성공. Tokens used: {}", response.getUsage().getTotalTokens());
                return content;
            } else {
                log.warn("OpenAI API 응답이 비어있습니다.");
                return "응답을 받지 못했습니다.";
            }

        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생: {}", e.getMessage(), e);
            return "오류가 발생했습니다: " + e.getMessage();
        }
    }

    /**
     * 마미톡잉글리시 프롬프트 생성
     */
    public String buildMommyTalkPrompt(String basePrompt, String theme, String context,
        int parentLevel, int childLevel) {
        return String.format("""
            %s
            
            **실제 입력**:
            주제: %s
            맥락: %s
            부모 레벨: %d
            아이 레벨: %d
            """, basePrompt, theme, context, parentLevel, childLevel);
    }
}
