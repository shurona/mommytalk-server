package com.shrona.mommytalk.user.application;

import static com.shrona.mommytalk.user.common.exception.UserErrorCode.DAILY_LIMIT_EXCEEDED;
import static com.shrona.mommytalk.user.common.exception.UserErrorCode.NO_ACTIVE_ENTITLEMENT;
import static com.shrona.mommytalk.user.common.exception.UserErrorCode.USER_NOT_FOUND;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.entitlement.infrastructure.query.UserEntitlementQueryRepository;
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

    private static final int DAILY_SENTENCE_LIMIT = 1;

    private final UserJpaRepository userRepository;
    private final UserEntitlementQueryRepository userEntitlementQueryRepository;

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

        // 이용권 체크: 채널과 유저 기준으로 활성 이용권이 하나라도 있는지 확인
        boolean hasEntitlement = userEntitlementQueryRepository.hasActiveEntitlement(
            channel.getId(), userId);

        if (!hasEntitlement) {
            throw new UserException(NO_ACTIVE_ENTITLEMENT);
        }

        // 일일 생성 제한 체크
        int todayCount = userSentenceHistoryQueryRepository.countTodaySentences(userId);
        int limit = "SPC".equals(userInfo.getDescription()) ? 30 : DAILY_SENTENCE_LIMIT;

        if (todayCount >= limit) {
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
