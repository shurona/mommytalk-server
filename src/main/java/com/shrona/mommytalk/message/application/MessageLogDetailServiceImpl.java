package com.shrona.mommytalk.message.application;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.entitlement.domain.UserEntitlement;
import com.shrona.mommytalk.entitlement.infrastructure.query.UserEntitlementQueryRepository;
import com.shrona.mommytalk.message.domain.MessageContent;
import com.shrona.mommytalk.message.domain.MessageLog;
import com.shrona.mommytalk.message.domain.MessageLogDetail;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageContentJpaRepository;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageLogJpaRepository;
import com.shrona.mommytalk.message.infrastructure.repository.query.MessageLogDetailQueryRepository;
import com.shrona.mommytalk.user.domain.User;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MessageLogDetailServiceImpl implements MessageLogDetailService {

    private final MessageLogDetailQueryRepository messageLogDetailQueryRepository;
    private final MessageLogJpaRepository messageLogJpaRepository;
    private final MessageContentJpaRepository messageContentJpaRepository;
    private final UserEntitlementQueryRepository userEntitlementQueryRepository;

    @Override
    public Page<MessageLogDetail> findLogDetailListByLogId(Long messageLogId, Pageable pageable) {
        return messageLogDetailQueryRepository.findMessageLogDetailListByLogId(
            messageLogId, pageable);
    }

    @Override
    @Transactional
    public int createLegacyDetails(Channel channel, Long entitlementId) {
        log.info("[레거시 MessageLogDetail 생성 시작] channelId={}, entitlementId={}",
            channel.getId(), entitlementId);

        LocalDate afterDate = LocalDate.of(2025, 11, 30); // 예시

        // 1. groupInfo='legacy' + entitlement_id 조건으로 MessageLog 조회
        List<MessageLog> legacyLogs = messageLogJpaRepository.findAll().stream()
            .filter(log -> "legacy".equals(log.getGroupInfo()))
            .filter(log -> log.getEntitlement() != null
                && log.getEntitlement().getId().equals(entitlementId))
            .filter(log -> log.getChannel().getId().equals(channel.getId()))
            .filter(log -> !log.getMessageType().getDeliveryTime().isBefore(afterDate))
            .toList();

        log.info("[레거시 MessageLog 조회 완료] 조회된 MessageLog 개수={}", legacyLogs.size());

        int totalCreated = 0;

        for (MessageLog messageLog : legacyLogs) {
            try {
                int created = processLegacyMessageLog(messageLog, channel, entitlementId);
                totalCreated += created;
            } catch (Exception e) {
                log.error("[MessageLogDetail 생성 실패] messageLogId={}, error={}",
                    messageLog.getId(), e.getMessage(), e);
            }
        }

        log.info("[레거시 MessageLogDetail 생성 완료] 총 {}건 생성", totalCreated);

        return totalCreated;
    }

    private int processLegacyMessageLog(MessageLog messageLog, Channel channel,
        Long entitlementId) {

        LocalDate deliveryDate = messageLog.getReserveTime().toLocalDate();

        log.info("[MessageLog 처리 시작] messageLogId={}, deliveryDate={}",
            messageLog.getId(), deliveryDate);

        // 2. childLevel=2, userLevel=2인 MessageContent 조회
        Optional<MessageContent> messageContentOpt = messageContentJpaRepository
            .findByMessageTypeAndChildLevelAndUserLevel(
                messageLog.getMessageType(), 2, 2);

        if (messageContentOpt.isEmpty()) {
            log.warn("[MessageContent 없음 - 스킵] messageLogId={}, date={}",
                messageLog.getId(), deliveryDate);
            return 0;
        }

        MessageContent messageContent = messageContentOpt.get();

        // 3. 이미 MessageLogDetail이 있는 유저 ID 조회 (중복 제외용)
        Set<Long> existingUserIds = new HashSet<>();
        List<MessageLogDetail> existingDetails = messageLogDetailQueryRepository
            .findMldListByStatusWithKakao(messageLog.getId(), null);
        existingDetails.forEach(detail -> existingUserIds.add(detail.getUser().getId()));

        log.info("[기존 MessageLogDetail 조회] messageLogId={}, 기존 유저 수={}",
            messageLog.getId(), existingUserIds.size());

        // 4. UserEntitlement를 먼저 조회 (채널, 상품, 날짜, kakaoUser 필터링)
        List<UserEntitlement> activeEntitlements = userEntitlementQueryRepository
            .findActiveEntitlementsByChannelAndType(channel.getId(), entitlementId, deliveryDate);

        log.info("[유효한 UserEntitlement 조회 완료] 조회된 개수={}", activeEntitlements.size());

        // 5. 배치 처리 (500명씩)
        int batchSize = 200;
        int createdCount = 0;

        for (int i = 0; i < activeEntitlements.size(); i += batchSize) {
            int end = Math.min(i + batchSize, activeEntitlements.size());
            List<UserEntitlement> batch = activeEntitlements.subList(i, end);

            log.info("[배치 처리 시작] batch={}/{}, size={}",
                (i / batchSize) + 1, (activeEntitlements.size() + batchSize - 1) / batchSize,
                batch.size());

            for (UserEntitlement entitlement : batch) {
                User user = entitlement.getUser();  // 이미 JOIN FETCH로 조회됨

                // 중복 체크
                if (existingUserIds.contains(user.getId())) {
                    continue;
                }

                // MessageLogDetail 생성
                MessageLogDetail detail = MessageLogDetail.createLogDetailForLegacy(
                    messageLog, user, messageContent);
                messageLog.addMessageLogDetailInfo(detail);
                createdCount++;
            }
        }

        log.info("[MessageLogDetail 생성 완료] messageLogId={}, 생성 개수={}",
            messageLog.getId(), createdCount);

        return createdCount;
    }
}
