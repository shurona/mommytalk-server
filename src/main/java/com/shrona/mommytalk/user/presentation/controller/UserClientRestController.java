package com.shrona.mommytalk.user.presentation.controller;

import static com.shrona.mommytalk.channel.common.exception.ChannelErrorCode.CHANNEL_NOT_FOUND;

import com.shrona.mommytalk.channel.application.ChannelService;
import com.shrona.mommytalk.channel.common.exception.ChannelException;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.common.annotation.CurrentUserId;
import com.shrona.mommytalk.common.dto.ApiResponse;
import com.shrona.mommytalk.openai.domain.UserSentenceHistory;
import com.shrona.mommytalk.user.application.UserSentenceService;
import com.shrona.mommytalk.user.application.UserService;
import com.shrona.mommytalk.user.common.exception.UserErrorCode;
import com.shrona.mommytalk.user.common.exception.UserException;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.presentation.dtos.request.GenerateSentenceRequestDto;
import com.shrona.mommytalk.user.presentation.dtos.request.SentenceHistorySearchRequestDto;
import com.shrona.mommytalk.user.presentation.dtos.request.UpdateOnboardingRequestBody;
import com.shrona.mommytalk.user.presentation.dtos.request.UpdateUserRequestDto;
import com.shrona.mommytalk.user.presentation.dtos.response.CreateUserSentenceResponseDto;
import com.shrona.mommytalk.user.presentation.dtos.response.UserClientResponseDto;
import com.shrona.mommytalk.user.presentation.dtos.response.UserSentenceListResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/client/users")
@RestController
public class UserClientRestController {

    private final ChannelService channelService;

    private final UserService userService;
    private final UserSentenceService userSentenceService;

    @GetMapping("/me")
    public ApiResponse<UserClientResponseDto> findUserInfo(
        @CurrentUserId Long userId,
        @RequestParam Long channelId
    ) {

        User user = userService.findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        return ApiResponse.success(UserClientResponseDto.from(user, channelId));
    }

    @GetMapping("/sentences")
    public ApiResponse<List<UserSentenceListResponseDto>> findSentenceList(
        @CurrentUserId Long userId,
        @Validated @ModelAttribute SentenceHistorySearchRequestDto searchRequest
    ) {

        List<UserSentenceHistory> userSentenceHistoryList
            = userSentenceService.findUserSentenceHistoryList(
            userId,
            searchRequest.year(),
            searchRequest.month()
        );

        return ApiResponse.success(
            userSentenceHistoryList.stream().map(
                sentenceHistory ->
                    UserSentenceListResponseDto.of(
                        sentenceHistory.getId(),
                        sentenceHistory.getSentence(),
                        sentenceHistory.getOutput(),
                        sentenceHistory.getGenerateDate()
                    )
            ).toList()
        );
    }

    @GetMapping("/sentences/{sentenceId}")
    public ApiResponse<?> findSentenceDetail(
        @CurrentUserId Long userId,
        @PathVariable Long sentenceId
    ) {

        return ApiResponse.success("");
    }

    @PostMapping("/onboarding")
    public void updateOnboarding(
        @CurrentUserId Long userId,
        @RequestBody UpdateOnboardingRequestBody requestBody) {

        userService.updateUserInfoByClient(
            userId,
            UpdateUserRequestDto.of(
                requestBody.childName(),
                requestBody.userLevel(),
                requestBody.childLevel()
            )
        );
    }

    @PostMapping("/sentences")
    public ApiResponse<CreateUserSentenceResponseDto> generateSentence(
        @CurrentUserId Long userId,
        @RequestBody GenerateSentenceRequestDto requestBody
    ) {
        Channel channel = channelService.findChannelById(requestBody.channelId())
            .orElseThrow(() -> new ChannelException(CHANNEL_NOT_FOUND));

        String sentence = userSentenceService.generateUserSentence(
            userId, channel, requestBody.sentence());

        return ApiResponse.success(new CreateUserSentenceResponseDto(sentence));
    }

}
