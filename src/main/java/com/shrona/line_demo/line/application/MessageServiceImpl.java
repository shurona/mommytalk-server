package com.shrona.line_demo.line.application;

import com.shrona.line_demo.line.application.utils.MessageUtils;
import com.shrona.line_demo.line.domain.Channel;
import com.shrona.line_demo.line.domain.MessageLog;
import com.shrona.line_demo.line.domain.MessageLogLineInfo;
import com.shrona.line_demo.line.domain.MessageType;
import com.shrona.line_demo.line.infrastructure.MessageLogJpaRepository;
import com.shrona.line_demo.line.infrastructure.MessageTypeJpaRepository;
import com.shrona.line_demo.line.infrastructure.dao.LogLineIdCount;
import com.shrona.line_demo.user.application.GroupService;
import com.shrona.line_demo.user.domain.Group;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        (Channel channel, Long messageTypeId, List<Long> groupIds, LocalDateTime reserveTime,
            String content) {

        Optional<MessageType> typeInfo = messageTypeRepository.findById(messageTypeId);
        List<Group> groupInfo = groupService.findGroupByIdList(groupIds);

        if (typeInfo.isEmpty() || groupInfo.isEmpty()) {
            return null;
        }

        List<MessageLog> messageLogList = messageLogRepository.saveAll(groupInfo.stream()
            .map(g -> createMessageLogForGroup(g, channel, typeInfo.get(), reserveTime, content))
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

        // todo: 추후에 그룹이 많아지면 loop으로 처리
        List<Group> groupInfo = groupService.findGroupListNotIn(channel, exceptGroupIds);

        if (typeInfo.isEmpty() || groupInfo.isEmpty()) {
            return null;
        }

        List<MessageLog> messageLogList = messageLogRepository.saveAll(groupInfo.stream()
            .map(g -> createMessageLogForGroup(g, channel, typeInfo.get(), reserveTime, content))
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
        return messageLogRepository.findAllByReservedMessage(LocalDateTime.now());
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

    /**
     * MessageLog를 생성해 주는 메소드
     */
    private MessageLog createMessageLogForGroup(Group g, Channel channel, MessageType type,
        LocalDateTime reserveTime, String content) {
        List<String> lineIds = groupService.findGroupById(g.getId(), true)
            .getUserGroupList()
            .stream()
            .filter(gu -> gu.getUser().getLineId() != null)
            .map(gu -> gu.getUser().getLineId())
            .toList();

        MessageLog messageLog = MessageLog.messageLog(channel, type, g, reserveTime, content);

        lineIds.forEach(l -> {
            messageLog.addMessageLogLineInfo(
                MessageLogLineInfo.createLineInfo(messageLog, l));
        });

        return messageLog;
    }
}
