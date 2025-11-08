package com.shrona.mommytalk.message.application;

import static com.shrona.mommytalk.group.common.exception.GroupErrorCode.GROUP_NOT_FOUND;
import static com.shrona.mommytalk.message.common.exception.MessageErrorCode.MESSAGE_CHANNEL_MISMATCH;
import static com.shrona.mommytalk.message.common.exception.MessageErrorCode.MESSAGE_NOT_SCHEDULED_FOR_DATE;
import static com.shrona.mommytalk.message.domain.type.ReservationStatus.FAIL;
import static com.shrona.mommytalk.message.domain.type.ReservationStatus.PREPARE;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.channel.domain.ChannelPlatform;
import com.shrona.mommytalk.entitlement.infrastructure.query.EntitlementQueryRepository;
import com.shrona.mommytalk.group.application.GroupService;
import com.shrona.mommytalk.group.common.exception.GroupException;
import com.shrona.mommytalk.group.domain.Group;
import com.shrona.mommytalk.group.infrastructure.repository.jpa.GroupJpaRepository;
import com.shrona.mommytalk.group.infrastructure.repository.query.GroupQueryRepository;
import com.shrona.mommytalk.kakao.application.sender.KakaoMessageSender;
import com.shrona.mommytalk.line.application.sender.LineMessageSender;
import com.shrona.mommytalk.line.infrastructure.dao.LogMessageIdCount;
import com.shrona.mommytalk.message.common.exception.MessageErrorCode;
import com.shrona.mommytalk.message.common.exception.MessageException;
import com.shrona.mommytalk.message.common.utils.MessageUtils;
import com.shrona.mommytalk.message.domain.MessageContent;
import com.shrona.mommytalk.message.domain.MessageLog;
import com.shrona.mommytalk.message.domain.MessageLogDetail;
import com.shrona.mommytalk.message.domain.MessageType;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageLogJpaRepository;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageTypeJpaRepository;
import com.shrona.mommytalk.message.infrastructure.repository.query.MessageLogDetailQueryRepository;
import com.shrona.mommytalk.message.infrastructure.repository.query.MessageLogQueryRepository;
import com.shrona.mommytalk.user.domain.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    // repository
    private final MessageLogJpaRepository messageLogRepository;
    private final MessageTypeJpaRepository messageTypeRepository;
    private final GroupJpaRepository groupJpaRepository;

    private final MessageLogQueryRepository messageLogQueryRepository;
    private final MessageLogDetailQueryRepository messageLogDetailQueryRepository;
    private final GroupQueryRepository groupQueryRepository;
    private final EntitlementQueryRepository entitlementQueryRepository;

    // service
    private final GroupService groupService;
    private final MessageContentService messageContentService;

    private final LineMessageSender lineMessageSender;
    private final KakaoMessageSender kakaoMessageSender;

    // Utils
    private final MessageUtils messageUtils;


    @Transactional
    public List<MessageLog> createMessageSelectGroup
        (Channel channel, Long selectGroupId,
            List<Long> selectedCustomGroupIds, List<Long> exceptGroupIds,
            LocalDateTime reserveTime, String groupInfo) {

        // 해당 날짜와 채널에 해당하는 MessageType 정보를 갖고 온다.
        MessageType typeInfo = messageTypeRepository.findByDeliveryTime(
                reserveTime.toLocalDate(), channel)
            .orElseThrow(() -> new MessageException(MESSAGE_NOT_SCHEDULED_FOR_DATE));

        Group entitlementGroupInfo = groupJpaRepository.findById(selectGroupId)
            .orElseThrow(() -> new GroupException(GROUP_NOT_FOUND));
        if (entitlementGroupInfo.getEntitlement() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "상품 정보가 없습니다.");
        }

        // 선택된 그룹에 속한 유저 정보를 갖고 온다.
        List<Long> groupList = new ArrayList<>(selectedCustomGroupIds);
        groupList.add(selectGroupId);
        List<User> userListByGroupIds = groupQueryRepository.findUserListByGroupIds(groupList);
        if (userListByGroupIds.isEmpty()) {
            return null;
        }

        // 제외할 UserIds를 갖고 온다.
        Set<Long> exceptUserIds = getExceptUserIds(exceptGroupIds);

        // 저장될 메세지 목록을 갖고 온다.
        MessageLog messageLogForSave = createMessageLogForGroup(
            channel, typeInfo, // MessageLog 생성 로직
            userListByGroupIds,
            reserveTime, groupInfo, exceptUserIds);

        // 상품 정보 업데이트
        messageLogForSave.updateMessageEntitlement(entitlementGroupInfo.getEntitlement());

        // messageLog 저장
        MessageLog messageLogInfo = messageLogRepository.save(messageLogForSave);

        // commit이 된 이후에 실행을 한다.
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {

                public void afterCommit() {
                    messageUtils.registerTaskSchedule(List.of(messageLogInfo), reserveTime);
                }
            }
        );

        return List.of(messageLogInfo);
    }

    @Transactional
    public List<MessageLog> createMessageAllGroup
        (Channel channel, List<Long> exceptGroupIds, LocalDateTime reserveTime, String groupInfo) {

        MessageType typeInfo = messageTypeRepository.findByDeliveryTime(
                reserveTime.toLocalDate(), channel)
            .orElseThrow(() -> new MessageException(MESSAGE_NOT_SCHEDULED_FOR_DATE));

        // todo: 유저 정보 set으로 갖고 오기
        List<User> allUserList = new ArrayList<>();

        if (allUserList.isEmpty()) {
            return null;
        }

        Set<Long> exceptUserIds = getExceptUserIds(exceptGroupIds);

        MessageLog messageLogInfo = createMessageLogForGroup(channel, typeInfo,
            allUserList,
            reserveTime, groupInfo, exceptUserIds);

        // commit이 된 이후에 실행을 한다.
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                
                public void afterCommit() {
                    messageUtils.registerTaskSchedule(List.of(messageLogInfo), reserveTime);
                }
            }
        );

        return List.of(messageLogInfo);
    }


    @Override
    public MessageLog findByMessageId(Long id) {
        return messageLogRepository.findById(id).orElse(null);
    }

    @Override
    public MessageLog findInfoByMessageId(Long id) {
        return messageLogQueryRepository.findMessageLogById(id);
    }

    @Override
    public Page<MessageLog> findMessageLogList(Channel channel, Pageable pageable) {
        return messageLogRepository.findAllByChannel(channel, pageable);
    }

    @Override
    public List<MessageLog> findAllByBeforeNow() {
        return messageLogRepository.findAllByBeforeNow(LocalDateTime.now());
    }

    @Override
    public Map<Long, Integer> findLineIdCountByLog(List<Long> logIds) {
        return messageLogQueryRepository.findMessageCountPerLog(logIds)
            .stream()
            .collect(Collectors.toMap(
                LogMessageIdCount::id,
                middle -> middle.count().intValue()
            ));
    }

    @Transactional
    public Long cancelMessage(Long messageLogId) {
        MessageLog messageLog = messageLogRepository.findById(messageLogId).orElseThrow(
            () -> new MessageException(MessageErrorCode.MESSAGE_LOG_NOT_FOUND)
        );

        // cancel
        messageLog.cancelMessageLog();

        messageLogDetailQueryRepository.cancelDetailByLogId(messageLogId);

        return messageLog.getId();
    }

    @Transactional
    public void resendMessage(Channel channel, Long messageLogId) {
        MessageLog messageLog = messageLogRepository.findById(messageLogId).orElseThrow(
            () -> new MessageException(MessageErrorCode.MESSAGE_LOG_NOT_FOUND)
        );

        if (messageLog.getReserveTime().isAfter(LocalDateTime.now())) {
            throw new MessageException(MessageErrorCode.MESSAGE_NOT_DELIVER_YET);
        }

        if (messageLog.getCancel()) {
            throw new MessageException(MessageErrorCode.MESSAGE_ALREADY_CANCEL);
        }

        if (!messageLog.getChannel().getId().equals(channel.getId())) {
            throw new MessageException(MESSAGE_CHANNEL_MISMATCH);
        }

        ChannelPlatform platform = messageLog.getChannel().getChannelPlatform();

        switch (platform) {
            case ChannelPlatform.KAKAO -> {
                kakaoMessageSender.sendKakaoMessageByReservationByMessageIds(
                    List.of(messageLogId), List.of(PREPARE, FAIL));
            }
            case ChannelPlatform.LINE -> {
                lineMessageSender.sendLineMessageByReservationByMessageIds(
                    List.of(messageLogId), List.of(PREPARE, FAIL));
            }
        }
    }

    /**
     * MessageLog를 생성해 주는 메소드
     */
    private MessageLog createMessageLogForGroup(Channel channel, MessageType type,
        List<User> userList, LocalDateTime reserveTime, String groupInfo, Set<Long> exceptUserIds) {

        // 보내지 않은 유저를 제외한 유저 목록을 생성한다.
        List<User> sendUserInfo = userList.stream()
            .filter(user -> !exceptUserIds.contains(user.getId())) // 제외 그룹 유저 한다.
            .toList();

        MessageLog messageLog = MessageLog.messageLog(
            channel, type, reserveTime, groupInfo);

        // MessageContent를 레벨 조합으로 미리 Map에 저장 (한 번만 조회)
        Map<String, MessageContent> levelMap = messageContentService
            .groupMessageContentByLevel(type);

        // 연관관계를 이용해서 MessageLogDetail 정보를 저장한다.
        sendUserInfo.forEach(user -> {
            String levelKey = user.createKeyPropertyForMessageContent();
            MessageContent messageContent = levelMap.get(levelKey);

            if (messageContent != null) {
                messageLog.addMessageLogDetailInfo(
                    MessageLogDetail.createLogDetail(messageLog, user, messageContent)
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
