package com.shrona.mommytalk.openai.presentation.controller;

import com.shrona.mommytalk.openai.application.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/channels/{channelId}")
public class OpenAiRestController {

    private final OpenAiService openAiService;


    @GetMapping("/test")
    public String testOpenAi() {
        return openAiService.testPrompt();
    }


}