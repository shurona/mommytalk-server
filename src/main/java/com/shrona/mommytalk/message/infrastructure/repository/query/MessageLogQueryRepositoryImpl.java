package com.shrona.mommytalk.message.infrastructure.repository.query;

import static com.shrona.mommytalk.message.domain.QMessageContent.messageContent;
import static com.shrona.mommytalk.message.domain.QMessageLog.messageLog;
import static com.shrona.mommytalk.message.domain.QMessageLogDetail.messageLogDetail;
import static com.shrona.mommytalk.message.domain.QMessageType.messageType;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.line.infrastructure.dao.LogMessageIdCount;
import com.shrona.mommytalk.message.domain.MessageLog;
import com.shrona.mommytalk.message.domain.MessageType;
import com.shrona.mommytalk.message.domain.type.ReservationStatus;
import com.shrona.mommytalk.message.presentation.dtos.response.AvailableDateResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.MessageLogResponseDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class MessageLogQueryRepositoryImpl implements MessageLogQueryRepository {

    private final JPAQueryFactory query;

    @Override
    public MessageLog findMessageLogById(Long messageId) {
        return query
            .selectFrom(messageLog)
            .leftJoin(messageLog.messageType, messageType).fetchJoin()
            .leftJoin(messageType.messageContentList, messageContent).fetchJoin()
            .where(
                messageLog.id.eq(messageId)
            ).fetchOne();
    }


    @Override
    public List<AvailableDateResponseDto> findAvailableMessageTypesWithFullApprovedContent(
        Channel channel, LocalDate startDate, LocalDate endDate) {

        // 1. 조건에 맞는 MessageType 조회
        List<MessageType> messageTypes = query
            .selectFrom(messageType)
            .where(
                messageType.channel.eq(channel),
                messageType.deliveryTime.goe(startDate),
                messageType.deliveryTime.loe(endDate),
                messageType.id.in(
                    // 9개 컨텐츠가 모두 승인된 MessageType ID만 선택
                    query.select(messageContent.messageType.id)
                        .from(messageContent)
//                        .where(messageContent.approved.eq(true))
                        .groupBy(messageContent.messageType.id)
//                        .having(messageContent.count().goe(9))
                        .having(messageContent.count().goe(0))
                )
            )
            .orderBy(messageType.deliveryTime.asc())
            .fetch();

        // 2. MessageType ID 목록 추출
        List<Long> messageTypeIds = messageTypes.stream()
            .map(MessageType::getId)
            .toList();

        // 3. MessageType별 승인된 Content 개수 조회
        Map<Long, Long> contentCountMap = new java.util.HashMap<>();
        if (!messageTypeIds.isEmpty()) {
            List<Tuple> countResults = query
                .select(
                    messageContent.messageType.id,
                    messageContent.count()
                )
                .from(messageContent)
                .where(
                    messageContent.messageType.id.in(messageTypeIds),
                    messageContent.approved.eq(true)
                )
                .groupBy(messageContent.messageType.id)
                .fetch();

            for (Tuple countRow : countResults) {
                Long typeId = countRow.get(messageContent.messageType.id);
                Long count = countRow.get(messageContent.count());
                contentCountMap.put(typeId, count);
            }
        }

        // 4. AvailableDateResponseDto로 변환
        return messageTypes.stream()
            .map(mt -> AvailableDateResponseDto.of(
                mt.getDeliveryTime().toString(),
                mt.getTheme(),
                contentCountMap.getOrDefault(mt.getId(), 0L).intValue()
            ))
            .toList();
    }

    @Override
    public Page<MessageLogResponseDto> findMessageLogsByChannel(Long channelId, Pageable pageable) {

        // 1. DB 레벨 페이징: MessageLog ID만 먼저 조회
        List<Long> messageLogIds = query
            .select(messageLog.id)
            .from(messageLog)
            .where(messageLog.channel.id.eq(channelId))
            .orderBy(messageLog.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        if (messageLogIds.isEmpty()) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0L);
        }

        // 2. 페이징된 ID에 대해서만 Detail과 함께 조회
        List<Tuple> rawResults = query
            .select(
                messageLog.id,
                messageType.id,
                messageType.theme,
                messageLog.createdAt,
                messageLog.reserveTime,
                messageLogDetail.status
            )
            .from(messageLog)
            .leftJoin(messageLog.messageType, messageType)
            .leftJoin(messageLog.messageLogDetailList, messageLogDetail)
            .where(messageLog.id.in(messageLogIds))
            .orderBy(messageLog.createdAt.desc())
            .fetch();

        // 3. 결과를 그룹화하고 상태별 개수 계산
        Map<Long, MessageLogData> dataMap = new LinkedHashMap<>();
        Map<Long, Long> messageLogToTypeMap = new HashMap<>();

        for (Tuple row : rawResults) {
            Long id = row.get(messageLog.id);
            Long messageTypeId = row.get(messageType.id);
            String theme = row.get(messageType.theme);
            LocalDateTime createdAt = row.get(messageLog.createdAt);
            LocalDateTime reserveTime = row.get(messageLog.reserveTime);
            ReservationStatus status = row.get(messageLogDetail.status);

            // MessageLog ID -> MessageType ID 매핑 저장
            messageLogToTypeMap.put(id, messageTypeId);

            // 첫 번째 row일 때 초기 데이터 생성
            dataMap.computeIfAbsent(id, k -> new MessageLogData(
                id, theme, createdAt, reserveTime
            ));

            // 상태별 개수 증가
            MessageLogData data = dataMap.get(id);
            if (status != null) {
                data.incrementStatus(status);
            }
        }

        // 4. 페이징된 결과의 MessageType ID 목록 추출
        List<Long> messageTypeIds = dataMap.values().stream()
            .map(data -> messageLogToTypeMap.get(data.id))
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        // 5. MessageType별 승인된 Content 개수 조회
        Map<Long, Long> contentCountMap = new HashMap<>();
        if (!messageTypeIds.isEmpty()) {
            List<Tuple> countResults = query
                .select(
                    messageContent.messageType.id,
                    messageContent.count()
                )
                .from(messageContent)
                .where(
                    messageContent.messageType.id.in(messageTypeIds),
                    messageContent.approved.eq(true)
                )
                .groupBy(messageContent.messageType.id)
                .fetch();

            for (Tuple countRow : countResults) {
                Long typeId = countRow.get(messageContent.messageType.id);
                Long count = countRow.get(messageContent.count());
                contentCountMap.put(typeId, count);
            }
        }

        // 6. 최종 DTO 변환
        List<MessageLogResponseDto> content = dataMap.values().stream()
            .<MessageLogResponseDto>map(data -> {
                Long messageTypeId = messageLogToTypeMap.get(data.id);
                Integer messageCount = contentCountMap.getOrDefault(messageTypeId, 0L).intValue();

                return MessageLogResponseDto.of(
                    data.id,
                    data.theme,
                    data.calculateOverallStatus(),
                    data.createdAt,
                    data.reserveTime,
                    messageCount,
                    data.successCount,
                    data.failCount,
                    data.getTotalCount()
                );
            })
            .collect(Collectors.toList());

        // 7. 전체 개수 조회
        Long total = query
            .select(messageLog.countDistinct())
            .from(messageLog)
            .where(messageLog.channel.id.eq(channelId))
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public List<LogMessageIdCount> findMessageCountPerLog(List<Long> ids) {
        List<Tuple> results = query
            .select(
                messageLog.id,
                messageLogDetail.count()
            )
            .from(messageLog)
            .leftJoin(messageLog.messageLogDetailList, messageLogDetail)
            .where(messageLog.id.in(ids))
            .groupBy(messageLog.id)
            .fetch();

        return results.stream()
            .map(tuple -> new LogMessageIdCount(
                tuple.get(messageLog.id),
                tuple.get(messageLogDetail.count())
            ))
            .toList();
    }

    /**
     * MessageLog의 상태별 개수를 추적하는 내부 클래스
     */
    private static class MessageLogData {

        final Long id;
        final String theme;
        final LocalDateTime createdAt;
        final LocalDateTime reserveTime;

        int prepareCount = 0;
        int successCount = 0;
        int failCount = 0;
        int cancelCount;

        MessageLogData(Long id, String theme,
            LocalDateTime createdAt,
            LocalDateTime reserveTime) {
            this.id = id;
            this.theme = theme;
            this.createdAt = createdAt;
            this.reserveTime = reserveTime;
        }

        void incrementStatus(ReservationStatus status) {
            switch (status) {
                case PREPARE -> prepareCount++;
                case COMPLETE -> successCount++;
                case FAIL -> failCount++;
                case CANCEL -> cancelCount++;
            }
        }

        int getTotalCount() {
            return prepareCount + successCount + failCount + cancelCount;
        }

        /**
         * 전체 상태 계산 (우선순위: PREPARE > FAIL > COMPLETE)
         */
        String calculateOverallStatus() {
            if (prepareCount > 0) {
                return "PREPARE";
            } else if (failCount > 0) {
                return "FAIL";
            } else {
                return "COMPLETE";
            }
        }
    }


}
