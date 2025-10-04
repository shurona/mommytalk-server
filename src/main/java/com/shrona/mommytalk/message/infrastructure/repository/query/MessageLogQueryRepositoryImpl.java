package com.shrona.mommytalk.message.infrastructure.repository.query;

import static com.shrona.mommytalk.message.domain.QMessageContent.messageContent;
import static com.shrona.mommytalk.message.domain.QMessageLog.messageLog;
import static com.shrona.mommytalk.message.domain.QMessageLogDetail.messageLogDetail;
import static com.shrona.mommytalk.message.domain.QMessageType.messageType;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.message.domain.MessageLog;
import com.shrona.mommytalk.message.domain.MessageType;
import com.shrona.mommytalk.message.domain.type.ReservationStatus;
import com.shrona.mommytalk.message.presentation.dtos.response.AvailableDateResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.MessageLogResponseDto;
import java.time.LocalDate;
import java.util.List;
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
        java.util.Map<Long, Long> contentCountMap = new java.util.HashMap<>();
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

        // 1. MessageLog와 상태 정보를 함께 조회
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
            .where(messageLog.channel.id.eq(channelId))
            .orderBy(messageLog.createdAt.desc())
            .fetch();

        // 2. 결과를 그룹화하고 상태 계산
        java.util.Map<Long, MessageLogResponseDto> resultMap = new java.util.LinkedHashMap<>();
        java.util.Map<Long, Long> messageLogToTypeMap = new java.util.HashMap<>();

        for (Tuple row : rawResults) {
            Long id = row.get(messageLog.id);
            Long messageTypeId = row.get(messageType.id);
            String theme = row.get(messageType.theme);
            java.time.LocalDateTime createdAt = row.get(messageLog.createdAt);
            java.time.LocalDateTime reserveTime = row.get(messageLog.reserveTime);
            ReservationStatus status = row.get(messageLogDetail.status);

            // MessageLog ID -> MessageType ID 매핑 저장
            messageLogToTypeMap.put(id, messageTypeId);

            resultMap.computeIfAbsent(id, k -> {
                return MessageLogResponseDto.of(id, theme, "COMPLETE", createdAt, reserveTime, 0);
            });

            // 상태 우선순위: PREPARE > FAIL > COMPLETE
            MessageLogResponseDto existing = resultMap.get(id);
            String currentStatus = existing.status();

            if (status == ReservationStatus.PREPARE ||
                (status == ReservationStatus.FAIL && !currentStatus.equals("PREPARE"))) {
                resultMap.put(id, MessageLogResponseDto.of(
                    id, theme, status.getStatus(), createdAt, reserveTime, 0));
            }
        }

        // 3. 페이징 적용
        List<MessageLogResponseDto> allResults = new java.util.ArrayList<>(resultMap.values());
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allResults.size());
        List<MessageLogResponseDto> pagedResults = allResults.subList(start, end);

        // 4. 페이징된 결과의 MessageType ID 목록 추출
        List<Long> messageTypeIds = pagedResults.stream()
            .map(dto -> messageLogToTypeMap.get(dto.id()))
            .filter(java.util.Objects::nonNull)
            .distinct()
            .toList();

        // 5. MessageType별 승인된 Content 개수 조회
        java.util.Map<Long, Long> contentCountMap = new java.util.HashMap<>();
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

        // 6. messageCount 업데이트
        List<MessageLogResponseDto> content = pagedResults.stream()
            .map(dto -> {
                Long messageTypeId = messageLogToTypeMap.get(dto.id());
                Integer messageCount = contentCountMap.getOrDefault(messageTypeId, 0L).intValue();
                return MessageLogResponseDto.of(
                    dto.id(),
                    dto.theme(),
                    dto.status(),
                    dto.createdAt(),
                    dto.deliveryDate(),
                    messageCount
                );
            })
            .toList();

        // 4. 전체 개수 조회
        Long total = query
            .select(messageLog.countDistinct())
            .from(messageLog)
            .where(messageLog.channel.id.eq(channelId))
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}
