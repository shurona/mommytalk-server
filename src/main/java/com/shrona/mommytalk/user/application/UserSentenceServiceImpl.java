package com.shrona.mommytalk.user.application;

import static com.shrona.mommytalk.user.common.exception.UserErrorCode.DAILY_LIMIT_EXCEEDED;
import static com.shrona.mommytalk.user.common.exception.UserErrorCode.USER_NOT_FOUND;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.openai.application.OpenAiService;
import com.shrona.mommytalk.openai.domain.MessagePrompt;
import com.shrona.mommytalk.openai.domain.UserSentenceHistory;
import com.shrona.mommytalk.openai.domain.type.PromptType;
import com.shrona.mommytalk.openai.infrastructure.repository.jpa.UserSentenceHistoryJpaRepository;
import com.shrona.mommytalk.openai.infrastructure.repository.query.MessagePromptQueryRepository;
import com.shrona.mommytalk.openai.infrastructure.repository.query.UserSentenceHistoryQueryRepository;
import com.shrona.mommytalk.user.common.exception.UserException;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.infrastructure.repository.jpa.UserJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserSentenceServiceImpl implements UserSentenceService {

    private final UserJpaRepository userRepository;

    private final UserSentenceHistoryJpaRepository userSentenceHistoryJpaRepository;
    private final UserSentenceHistoryQueryRepository userSentenceHistoryQueryRepository;
    private final MessagePromptQueryRepository messagePromptQueryRepository;
    private final OpenAiService openAiService;

    @Override
    public List<UserSentenceHistory> findUserSentenceHistoryList(Long userId, Integer year,
        Integer month) {
        return userSentenceHistoryQueryRepository.findSentenceListByUser(userId, year, month);
    }

    @Override
    public UserSentenceHistory findUserSentenceDetail(Long userId, Long sentenceId) {
        return null;
    }


    @Transactional
    public String generateUserSentence(Long userId, Channel channel, String sentence) {

        User userInfo = userRepository.findById(userId)
            .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        UserSentenceHistory recentHistory = userSentenceHistoryQueryRepository.findRecentHistory(
            userId);

        // 일부 유저의 경우 패스
        List<Long> passUser = List.of(52L, 152L);
        if (passUser.contains(userId)) {
            recentHistory = null;
        }

        // 날짜 기준 하루에 하나만 생성 가능합니다.
        if (recentHistory != null && recentHistory.isPast()) {
            throw new UserException(DAILY_LIMIT_EXCEEDED);
        }

        MessagePrompt messagePrompt = messagePromptQueryRepository.findSelectedPromptByChannelAndType(
            channel, PromptType.ADVANCE);

        UserSentenceHistory userSentenceHistory = userSentenceHistoryJpaRepository.save(
            UserSentenceHistory.of(userInfo, sentence));

        String output = openAiService.generateDataByUser(messagePrompt, userSentenceHistory);

        userSentenceHistory.updateOutput(output);

        return output;
    }
}
