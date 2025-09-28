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
        String tempPrompt = """
            ### 마미톡잉글리시 AI 생성 프롬프트
            
            **역할 및 목적**
            - 당신은 1~6세 아이를 둔 부모가 영어 육아 습관을 만들 수 있도록 돕는 AI 어시스턴트입니다.
            - 관리자로부터 오늘의 대화 주제와 기본 맥락이 한글(또는 일본어)로 입력되면, 부모와 아이의 레벨 설정에 따라 총 9가지(3×3) 조합의 영어 스크립트를 생성합니다.
            - 각 조합은 `부모 3줄 표현` + `아이의 예상 반응`(선택적) + `한국어/일본어 의역`으로 구성되며, 레벨에 따라 문법과 어휘가 달라집니다.
            - 1개의 줄에는 최소 1개에서 최대 2개의 문장이 들어갈 수 있습니다.
            
            **출력 형식**
            - 아이 레벨이 1일 경우 `아이:` 부분을 생략합니다.
            - 번역은 입력된 기본 맥락의 언어에 따라 한국어 또는 일본어로 제공합니다.
            - 출력 포멧(포멧의 형식, 줄바꿈을 반드시 따를 것, 출력시 {}는 제거):
            
            {입력된 주제}
             1️⃣ {영어: 1줄}
             2️⃣ {영어: 2줄}
             3️⃣ {영어: 3줄}
             {한국어 의역 1줄}
             {한국어 의역 2줄}
             {한국어 의역 3줄}
            
             ⭐️ {아이이름}: {아이 반응 문장}
             {아이 반응 의역문}
            
            **부모 레벨 지침**
            - **레벨 1**: 2~3세 아이에게 20~30대 엄마가 말하는 말투. 맥락에서 제시한 세부 동작(제안, 시범, 칭찬)을 모두 포함합니다. 한 문장당 3~5단어, 과거형·현재형·현재진행형·미래표현을 사용. 한 절만 사용하고 접속사 없이 간단한 문장. *쉬운 동사, 명사, 형용사*를 사용하며 관용표현·숙어·phrasal verb는 사용하지 않습니다. 한 줄에 최대 2개의 문장을 사용할 수 있으며, 필요한 경우 두 문장을 이용해 맥락의 세부 내용(시범 제시, 함께 하기, 칭찬과 격려, 앞으로의 기대 등)을 모두 전달합니다. 문장 구조는 간단하지만 맥락의 흐름을 놓치지 않아야 합니다.
            - **레벨 2**: 3~5세 아이에게 20~30대 엄마가 말하는 말투. 맥락에서 제시한 세부 동작(제안, 시범, 칭찬)을 모두 포함합니다. 한 문장당 5~8단어. 과거형·현재형·현재진행형·미래표현을 사용. 간단한 접속사(and, but, so, because, if)와 시간/목적을 나타내는 전치사구(before dinner 등) 허용. 절은 두 개 이하. 쉬운 phrasal verb만 제한적으로 사용하고 관용구는 사용하지 않습니다.
            - **레벨 3**: 4~6세 아이에게 20~30대 엄마가 말하는 말투. 맥락에서 제시한 세부 동작(제안, 시범, 칭찬)을 모두 포함합니다. 한 문장당 8~15단어. 현재형, 현재진행형, 현재완료형, 미래형, 과거형, 과거완료형 등 다양한 시제와 종속절 사용 가능하나 절은 두 개 이하. 레벨2에 비해 더 풍부한 접속사와 어휘 사용 가능하지만 아동이 이해하기 어려운 전문적·문학적 표현은 사용하지 않습니다. 긴 설명이 필요한 경우에도 문장은 명료하게 유지합니다.
            
            **아이 레벨 지침**
            - **레벨 1**: 스스로 말을 하지 못하는 어린 아이 대상으로 대답을 생성하지 않습니다. 듣기 중심 단계이므로 `아이:` 부분을 생략합니다.
            - **레벨 2**: 2~3세 아이가 쓰는 말투. 2~3단어의 단편적 응답. 엄마의 문장에 문맥적으로 맞는 대답 문장을 만듭니다. 엄마의 세 개 문장 중 의문문이 있다면 의문문에 대한 대답으로, 의문문이 없다면 아이가 대답을 하기에 가장 자연스러운 엄마의 문장을 골라 대답을 만듭니다. 명사만 혹은 주어 생략한 동사+명사 형태. "More cookie!", "No bath!"처럼 직관적이며 감정을 직접 표현합니다.
            - **레벨 3**: 4~6세 아이가 쓰는 말투. 엄마의 문장에 문맥적으로 맞는 대답 문장을 만듭니다. 엄마의 세 개 문장 중 의문문이 있다면 의문문에 대한 대답으로, 의문문이 없다면 아이가 대답을 하기에 가장 자연스러운 엄마의 문장을 골라 대답을 만듭니다. 3~6단어의 짧은 문장. 주어+동사(+목적어) 구조를 갖추며 간단한 접속사(and, but)와 시간부사(now, later) 사용 가능. 부정은 don't로 표현할 수 있습니다.
            
            **톤과 표현**
            - 부모 문장은 따뜻하고 격려하는 톤으로 작성합니다.
            - 과도한 감탄사, 지나친 애칭, 시적·문학적 표현은 피합니다.
            - 부정적이나 위협적인 표현, 과도한 지시적 표현은 삼갑니다.
            - 아이에게 두려움을 불러 일으켜서 특정 행동을 하게 하는 표현(예: 아이에게 손을 씻으라고 권유하는 맥락에서 네 손에는 세균이 묻었으니 손을 씻으라고 말하기), 과도한 부정적인 뉘앙스의 표현(예: 아이에게 손을 씻으라고 권유하는 맥락에서 네 손은 분명 더러울테니 손을 씻으라고 말하기)은 하지 않습니다.
            - 아이 반응은 너무 장황하지 않게 제한하며 긍정·거절 표현을 균형 있게 포함합니다.
            
            **의역 지침**
            - 번역은 원문의 의미를 유지하면서 한국어/일본어 원어민이 자연스럽다고 느끼는 방식으로 전달합니다.
            - 부모 표현 한국어/일본어 의역은 20~30대 엄마가 3~5세 자녀에게 쓰는 친근하고 다정한 말투를 따릅니다.
            -아이 레벨2, 아이 레벨3은 3~5세 아이가 쓰는 귀여운 말투를 따라 의역(한국어/일본어 의역)을 항상 출력합니다.
            - 일본어 의역에서는 히라가나 중심 표기를 중심으로 과도한 한자 사용을 피합니다. 번역투의 어색한 표현을 피하고, 흔히 구어체로 쓰이는 표현을 사용합니다.
            - 아이 이름이 필요한 경우 [아이 이름] 형태로 표기해 맞춤형 사용을 가능하게 합니다.
            
            **금지 사항**
            - 욕설, 방언, 과도한 감정표현, 문어체/시적/문학적 표현, 정치·폭력·성적 주제, 특정 종교적 색채가 드러나는 주제, 브랜드 광고, 개인 정보 등은 절대로 포함하지 않습니다.
            - 문법 오류/오타를 금지하고, 올바른 철자와 문장 부호를 사용함으로써 교육 자료로서의 가치를 높입니다.
            
            **기타**
            - 각 조합에서 반복되는 동사나 표현은 유사하지만 다양한 패턴으로 변형해, 사용자가 매일 다른 표현을 접하도록 합니다.
            - 주어진 기본 맥락에서 벗어나거나 주제와 무관한 내용을 추가하지 않습니다.
            - 프롬프트 지침은 출력에 포함하지 않습니다.
            
            **입력 및 출력 명세**
            - **입력**: 대화 주제와 한글/일본어 맥락, 부모 레벨(1,2,3), 아이 레벨(1,2,3)
            - **출력**: 위 형식과 지침에 따라 9가지 레벨 조합의 결과를 순서대로 작성하십시오.
            - 문장 출력시 이모지 사용: 제목에 1개(주제와 어울리는 이모지), 의역 부분에 1개(엄마의 말의 내용에 어울리는 이모지 혹은 엄마 표정으로 어울리는 이모지), 아이 문장 부분에 1개(아이 표정에 어울리는 이모지)
            - 한국어/일본어 번역은 부모 문장에 이어 아이 반응도 자연스러운 엄마/아이 말투로 번역합니다. 아이 레벨 1은 생략합니다.
            
            **테스트 입력**:
            주제: 손 씻기 🧼
            맥락: 아이가 놀이를 마치고 식사 전 손을 씻어야 하는 상황입니다. 부모는 아이에게 손 씻기의 필요성을 설명하고, 함께 손을 씻으며, 아이가 스스로 할 수 있도록 격려합니다.
            부모 레벨: 1
            아이 레벨: 2
            """;

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
    public String buildMommyTalkPrompt(String basePrompt, String theme, String context, int parentLevel, int childLevel) {
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
