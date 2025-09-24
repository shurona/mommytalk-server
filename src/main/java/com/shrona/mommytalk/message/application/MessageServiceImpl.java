package com.shrona.mommytalk.message.application;

import static com.shrona.mommytalk.message.common.exception.MessageErrorCode.MESSAGE_NOT_SCHEDULED_FOR_DATE;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.group.application.GroupService;
import com.shrona.mommytalk.group.domain.Group;
import com.shrona.mommytalk.group.domain.UserGroup;
import com.shrona.mommytalk.line.infrastructure.dao.LogMessageIdCount;
import com.shrona.mommytalk.message.common.exception.MessageException;
import com.shrona.mommytalk.message.common.utils.MessageUtils;
import com.shrona.mommytalk.message.domain.MessageLog;
import com.shrona.mommytalk.message.domain.MessageLogDetail;
import com.shrona.mommytalk.message.domain.MessageTemplate;
import com.shrona.mommytalk.message.domain.MessageType;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageLogJpaRepository;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageTypeJpaRepository;
import com.shrona.mommytalk.user.domain.User;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    // repository
    private final MessageLogJpaRepository messageLogRepository;
    private final MessageTypeJpaRepository messageTypeRepository;

    // service
    private final GroupService groupService;

    // Utils
    private final MessageUtils messageUtils;


    @Transactional
    public List<MessageLog> createMessageSelectGroup
        (Channel channel, Long messageTypeId,
            List<Long> selectedGroupIds, List<Long> exceptGroupIds,
            LocalDateTime reserveTime, String content) {

        MessageType typeInfo = messageTypeRepository.findByDeliveryTime(reserveTime.toLocalDate())
            .orElseThrow(() -> new MessageException(MESSAGE_NOT_SCHEDULED_FOR_DATE));

        List<Group> groupInfo = groupService.findGroupByIdList(selectedGroupIds);

        if (groupInfo.isEmpty()) {
            return null;
        }

        // 제외할 UserIds를 갖고 온다.
        Set<Long> exceptUserIds = getExceptUserIds(exceptGroupIds);

        List<MessageLog> messageLogList = messageLogRepository.saveAll(groupInfo.stream()
            .map(g -> createMessageLogForGroup(g, channel, typeInfo,
                reserveTime, content, exceptUserIds))
            .toList());

        // commit이 된 이후에 실행을 한다.
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    messageUtils.registerTaskSchedule(messageLogList, reserveTime);
                }
            }
        );

        return messageLogList;
    }

    @Transactional
    public List<MessageLog> createMessageAllGroup
        (Channel channel, Long messageTypeId, List<Long> exceptGroupIds, LocalDateTime reserveTime,
            String content) {

        MessageType typeInfo = messageTypeRepository.findByDeliveryTime(reserveTime.toLocalDate())
            .orElseThrow(() -> new MessageException(MESSAGE_NOT_SCHEDULED_FOR_DATE));

        // todo: 추후에 그룹이 많아지면 loop으로 처리
        List<Group> groupInfo = groupService.findGroupListNotIn(channel, exceptGroupIds);

        if (groupInfo.isEmpty()) {
            return null;
        }

        Set<Long> exceptUserIds = getExceptUserIds(exceptGroupIds);

        List<MessageLog> messageLogList = messageLogRepository.saveAll(groupInfo.stream()
            .map(g -> createMessageLogForGroup(g, channel, typeInfo,
                reserveTime, content, exceptUserIds))
            .toList());

        // commit이 된 이후에 실행을 한다.
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    messageUtils.registerTaskSchedule(messageLogList, reserveTime);
                }
            }
        );

        return messageLogList;
    }

    @Override
    public MessageLog findByMessageId(Long id) {
        return messageLogRepository.findById(id).orElse(null);
    }

    @Override
    public Page<MessageLog> findMessageLogList(Channel channel, Pageable pageable) {

        return messageLogRepository.findAllByChannel(channel, pageable);
    }

    @Override
    public List<MessageLog> findReservedMessage(Channel channel) {
        return messageLogRepository.findAllReservedMessageByChannel(channel, LocalDateTime.now());
    }

    @Override
    public List<MessageLog> findAllByBeforeNow() {
        return messageLogRepository.findAllByBeforeNow(LocalDateTime.now());
    }

    @Override
    public Map<Long, Integer> findLineIdCountByLog(List<Long> logIds) {
        // 사용처
        return messageLogRepository.findMessageCountPerLog(logIds)
            .stream()
            .collect(Collectors.toMap(
                LogMessageIdCount::id,
                middle -> middle.count().intValue()
            ));
    }

    @Transactional
    public MessageLog updateMessageLog(Long messageId, String content) {

        return messageLogRepository.findById(messageId)
            .map(messageLog -> {
                messageLog.updateMessage(content);
                return messageLog;
            })
            .orElseThrow(() -> {
                log.error("메시지 업데이트 실패: 존재하지 않는 messageId={}", messageId);
                return new IllegalArgumentException("존재하지 않는 메시지 ID: " + messageId);
            });
    }

    /**
     * MessageLog를 생성해 주는 메소드
     */
    private MessageLog createMessageLogForGroup(Group g, Channel channel, MessageType type,
        LocalDateTime reserveTime, String content, Set<Long> exceptUserIds) {
        List<UserGroup> userGroupList = groupService.findGroupById(g.getId(), true)
            .getUserGroupList();

        List<User> sendUserInfo = groupService.findGroupById(g.getId(), true)
            .getUserGroupList()
            .stream()
            .map(UserGroup::getUser) // 유저 추출
            .filter(user -> !exceptUserIds.contains(
                user.getId())) // 제외 그룹 유저 한다.
            .toList();

        MessageLog messageLog = MessageLog.messageLog(channel, type, g, reserveTime, content);

        // MessageTemplate를 레벨 조합으로 미리 Map에 저장 (한 번만 조회)
        Map<String, MessageTemplate> levelMap = type.getMessageTemplateList()
            .stream()
            .collect(Collectors.toMap(
                smt -> smt.getUserLevel() + "_" + smt.getChildLevel(), // key: "1_2"
                smt -> smt
            ));

        sendUserInfo.forEach(user -> {
            String levelKey = user.getUserLevel() + "_" + user.getChildLevel();
            MessageTemplate messageTemplate = levelMap.get(levelKey);

            if (messageTemplate != null) {
                messageLog.addMessageLogLineInfo(
                    MessageLogDetail.createLogDetail(messageLog, user, messageTemplate)
                );
            }
        });

        return messageLog;
    }

    /**
     * 제외할 User Id 목록을 갖고 온다.
     */
    private Set<Long> getExceptUserIds(List<Long> selectedExGroupIds) {
        Set<Long> exceptUserIds;
        if (selectedExGroupIds != null) {
            exceptUserIds = new HashSet<>(groupService.findUserIdsByGroupIds(selectedExGroupIds));
        } else {
            exceptUserIds = new HashSet<>();
        }
        return exceptUserIds;
    }

}
