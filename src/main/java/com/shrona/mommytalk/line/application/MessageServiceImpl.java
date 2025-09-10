package com.shrona.mommytalk.line.application;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.line.application.utils.MessageUtils;
import com.shrona.mommytalk.line.domain.MessageLog;
import com.shrona.mommytalk.line.domain.MessageLogLineInfo;
import com.shrona.mommytalk.line.domain.MessageType;
import com.shrona.mommytalk.line.infrastructure.MessageLogJpaRepository;
import com.shrona.mommytalk.line.infrastructure.MessageTypeJpaRepository;
import com.shrona.mommytalk.line.infrastructure.dao.LogLineIdCount;
import com.shrona.mommytalk.user.application.GroupService;
import com.shrona.mommytalk.user.domain.Group;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    public MessageType createMessageType(String title, String text) {
        Optional<MessageType> mt = messageTypeRepository.findByTitle(title);
        MessageType messageType = MessageType.of(title, text);
        return mt.orElseGet(() -> messageTypeRepository.save(messageType));
    }

    @Transactional
    public List<MessageLog> createMessageSelectGroup
        (Channel channel, Long messageTypeId,
            List<Long> selectedGroupIds, List<Long> selectedExGroupIds,
            LocalDateTime reserveTime, String content) {

        // 제외할 LineIds를 갖고 온다.
        Set<String> exceptLineIds = getExceptLineIds(selectedExGroupIds);

        Optional<MessageType> typeInfo = messageTypeRepository.findById(messageTypeId);
        List<Group> groupInfo = groupService.findGroupByIdList(selectedGroupIds);

        if (typeInfo.isEmpty() || groupInfo.isEmpty()) {
            return null;
        }

        List<MessageLog> messageLogList = messageLogRepository.saveAll(groupInfo.stream()
            .map(g -> createMessageLogForGroup(g, channel, typeInfo.get(),
                reserveTime, content, exceptLineIds))
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

        Optional<MessageType> typeInfo = messageTypeRepository.findById(messageTypeId);

        Set<String> exceptLineIds = getExceptLineIds(exceptGroupIds);

        // todo: 추후에 그룹이 많아지면 loop으로 처리
        List<Group> groupInfo = groupService.findGroupListNotIn(channel, exceptGroupIds);

        if (typeInfo.isEmpty() || groupInfo.isEmpty()) {
            return null;
        }

        List<MessageLog> messageLogList = messageLogRepository.saveAll(groupInfo.stream()
            .map(g -> createMessageLogForGroup(g, channel, typeInfo.get(),
                reserveTime, content, exceptLineIds))
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
    public List<MessageLog> findReservedAllMessage() {
        return messageLogRepository.findAllByReservedMessageBeforeNow(LocalDateTime.now());
    }

    @Override
    public Map<Long, Integer> findLineIdCountByLog(List<Long> logIds) {
        // 사용처
        return messageLogRepository.findLineCountPerLog(logIds)
            .stream()
            .collect(Collectors.toMap(
                LogLineIdCount::id,
                middle -> middle.count().intValue()
            ));
    }

    @Transactional
    public MessageLog cancelSendMessage(Long messageId) {

        return messageLogRepository.findById(messageId)
            .map(messageLog -> {
                messageLog.cancelStatusBeforeSend();
                return messageLog;
            })
            .orElseThrow(() -> {
                log.error("메시지 발송 취소 실패: 존재하지 않는 messageId={}", messageId);
                return new IllegalArgumentException("존재하지 않는 메시지 ID: " + messageId);
            });

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
        LocalDateTime reserveTime, String content, Set<String> exceptLineIds) {
        List<String> lineIds = groupService.findGroupById(g.getId(), true)
            .getUserGroupList()
            .stream()
            .filter(gu -> gu.getUser().getLineUser() != null) // 라인 아이디가 있는 경우
            .filter(gu -> !exceptLineIds.contains(
                gu.getUser().getLineUser().getLineId())) // 제외 라인아이디에 없는 경우
            .map(gu -> gu.getUser().getLineUser().getLineId()) // 라인 아이디를 추출한다.
            .toList();

        MessageLog messageLog = MessageLog.messageLog(channel, type, g, reserveTime, content);

        lineIds.forEach(l -> {
            messageLog.addMessageLogLineInfo(
                MessageLogLineInfo.createLineInfo(messageLog, l));
        });

        return messageLog;
    }

    /**
     * 제외할 라인 Id 목록을 갖고 온다.
     */
    private Set<String> getExceptLineIds(List<Long> selectedExGroupIds) {
        Set<String> exceptLineIds;
        if (selectedExGroupIds != null) {
            exceptLineIds = new HashSet<>(groupService.findLineIdsByGroupIds(selectedExGroupIds));
        } else {
            exceptLineIds = new HashSet<>();
        }
        return exceptLineIds;
    }

}
