package com.shrona.mommytalk.entitlement.application;

import static com.shrona.mommytalk.entitlement.common.exception.EntitlementErrorCode.AUTO_ACTIVE_GROUP_NOT_FOUND;
import static com.shrona.mommytalk.entitlement.common.exception.EntitlementErrorCode.AUTO_ENDED_GROUP_NOT_FOUND;
import static com.shrona.mommytalk.entitlement.common.exception.EntitlementErrorCode.ENTITLEMENT_NOT_FOUND;
import static com.shrona.mommytalk.entitlement.common.exception.EntitlementErrorCode.USER_ENTITLEMENT_ALREADY_EXISTS;
import static com.shrona.mommytalk.entitlement.common.exception.EntitlementErrorCode.USER_ENTITLEMENT_NOT_FOUND;
import static com.shrona.mommytalk.group.domain.GroupType.AUTO_ACTIVE;
import static com.shrona.mommytalk.group.domain.GroupType.AUTO_ENDED;
import static com.shrona.mommytalk.user.common.exception.UserErrorCode.USER_NOT_FOUND;

import com.shrona.mommytalk.channel.application.ChannelService;
import com.shrona.mommytalk.channel.common.exception.ChannelException;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.common.utils.DateTimeUtils;
import com.shrona.mommytalk.entitlement.common.exception.EntitlementException;
import com.shrona.mommytalk.entitlement.domain.Entitlement;
import com.shrona.mommytalk.entitlement.domain.EntitlementStatus;
import com.shrona.mommytalk.entitlement.domain.UserEntitlement;
import com.shrona.mommytalk.entitlement.infrastructure.jpa.EntitlementJpaRepository;
import com.shrona.mommytalk.entitlement.infrastructure.jpa.UserEntitlementJpaRepository;
import com.shrona.mommytalk.entitlement.infrastructure.query.UserEntitlementQueryRepository;
import com.shrona.mommytalk.entitlement.presentation.dtos.request.AddUserEntitlementRequestDto;
import com.shrona.mommytalk.entitlement.presentation.dtos.request.BulkUpdateUserEntitlementRequestDto;
import com.shrona.mommytalk.entitlement.presentation.dtos.request.UpdateUserEntitlementRequestDto;
import com.shrona.mommytalk.entitlement.presentation.dtos.response.UserEntitlementResponseDto;
import com.shrona.mommytalk.group.domain.Group;
import com.shrona.mommytalk.group.domain.UserGroup;
import com.shrona.mommytalk.group.infrastructure.repository.jpa.UserGroupJpaRepository;
import com.shrona.mommytalk.group.infrastructure.repository.query.GroupQueryRepository;
import com.shrona.mommytalk.group.infrastructure.repository.query.UserGroupQueryRepository;
import com.shrona.mommytalk.user.common.exception.UserException;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.infrastructure.repository.jpa.UserJpaRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserEntitlementServiceImpl implements UserEntitlementService {

    private final UserEntitlementJpaRepository userEntitlementJpaRepository;
    private final UserEntitlementQueryRepository userEntitlementQueryRepository;
    private final EntitlementJpaRepository entitlementJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final UserGroupJpaRepository userGroupJpaRepository;
    private final UserGroupQueryRepository userGroupQueryRepository;
    private final GroupQueryRepository groupQueryRepository;
    private final ChannelService channelService;

    @Override
    @Transactional
    public UserEntitlement addUserEntitlement(AddUserEntitlementRequestDto requestDto) {
        // 1. 엔티티 조회
        User user = userJpaRepository.findById(requestDto.userId())
            .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        Channel channel = channelService.findChannelById(requestDto.channelId())
            .orElseThrow(() -> new ChannelException(
                com.shrona.mommytalk.channel.common.exception.ChannelErrorCode.CHANNEL_NOT_FOUND));

        Entitlement entitlement = entitlementJpaRepository.findById(requestDto.entitlementId())
            .orElseThrow(() -> new EntitlementException(ENTITLEMENT_NOT_FOUND));

        // 2. 중복 체크 (같은 유저가 같은 상품권을 이미 가지고 있는지)
        List<UserEntitlement> existing = userEntitlementQueryRepository
            .findActiveEntitlementsByType(user.getId(), entitlement.getId(),
                DateTimeUtils.todayKst());

        if (!existing.isEmpty()) {
            throw new EntitlementException(USER_ENTITLEMENT_ALREADY_EXISTS);
        }

        // 3. UserEntitlement 생성
        UserEntitlement userEntitlement = UserEntitlement.createUserEntitlement(
            user,
            entitlement,
            channel,
            requestDto.startDate(),
            requestDto.endDate()
        );

        // 상태가 INACTIVE로 전달되면 비활성으로 생성
        if (requestDto.status() == EntitlementStatus.INACTIVE) {
            userEntitlement.deactivate();
        }

        userEntitlementJpaRepository.save(userEntitlement);

        // 4. 상태가 ACTIVE인 경우에만 AUTO_ACTIVE 그룹에 추가
        if (userEntitlement.getStatus() == EntitlementStatus.ACTIVE) {
            Group autoActiveGroup = groupQueryRepository
                .findByChannelAndEntitlementAndGroupType(channel, entitlement, AUTO_ACTIVE)
                .orElseThrow(() -> new EntitlementException(AUTO_ACTIVE_GROUP_NOT_FOUND));

            UserGroup userGroup = UserGroup.createUserGroup(user, autoActiveGroup);
            userGroupJpaRepository.save(userGroup);

            log.info("[상품권 추가 - AUTO_ACTIVE 그룹 할당] userId={}, groupId={}",
                user.getId(), autoActiveGroup.getId());
        } else {
            log.info("[상품권 추가 - INACTIVE 상태로 생성, 그룹 미할당] userId={}", user.getId());
        }

        log.info("[상품권 추가] userId={}, entitlementId={}, status={}, startDate={}, endDate={}",
            user.getId(), entitlement.getId(), userEntitlement.getStatus(),
            userEntitlement.getStartDate(), userEntitlement.getEndDate());

        return userEntitlement;
    }

    @Override
    @Transactional
    public UserEntitlement updateUserEntitlement(
        Long userEntitlementId,
        UpdateUserEntitlementRequestDto requestDto
    ) {
        // 1. UserEntitlement 조회
        UserEntitlement userEntitlement = userEntitlementJpaRepository.findById(userEntitlementId)
            .orElseThrow(() -> new EntitlementException(USER_ENTITLEMENT_NOT_FOUND));

        // 2. 종료일 검증 (과거 날짜 불가)
//        if (requestDto.endDate().isBefore(DateTimeUtils.todayKst())) {
//            throw new EntitlementException(INVALID_END_DATE);
//        }

        // 3. 종료일 연장
        userEntitlement.updateEndDate(requestDto.endDate());

        // 4. 상태 변경 및 그룹 이동 (요청에 포함된 경우)
        if (requestDto.status() != null) {
            EntitlementStatus oldStatus = userEntitlement.getStatus();
            EntitlementStatus newStatus = requestDto.status();

            // 상태 업데이트
            switch (newStatus) {
                case ACTIVE -> userEntitlement.activate();
                case INACTIVE -> userEntitlement.deactivate();
                case EXPIRED -> userEntitlement.expire();
            }

            // ACTIVE → INACTIVE/EXPIRED: AUTO_ACTIVE → AUTO_ENDED 그룹 이동
            if (oldStatus == EntitlementStatus.ACTIVE
                && (newStatus == EntitlementStatus.INACTIVE
                || newStatus == EntitlementStatus.EXPIRED)) {

                moveToAutoEndedGroup(userEntitlement.getUser(), userEntitlement.getEntitlement());

                log.info("[상품권 수정 - 그룹 이동] userId={}, {} → {}, AUTO_ACTIVE → AUTO_ENDED",
                    userEntitlement.getUser().getId(), oldStatus, newStatus);
            }
            // INACTIVE/EXPIRED → ACTIVE: AUTO_ENDED → AUTO_ACTIVE 그룹 이동
            else if (
                (oldStatus == EntitlementStatus.INACTIVE || oldStatus == EntitlementStatus.EXPIRED)
                    && newStatus == EntitlementStatus.ACTIVE) {

                moveToAutoActiveGroup(userEntitlement.getUser(), userEntitlement.getEntitlement());

                log.info("[상품권 수정 - 그룹 이동] userId={}, {} → {}, AUTO_ENDED → AUTO_ACTIVE",
                    userEntitlement.getUser().getId(), oldStatus, newStatus);
            }
        }

        log.info("[상품권 수정] userEntitlementId={}, status={}, endDate={}",
            userEntitlementId, requestDto.status(), requestDto.endDate());

        return userEntitlement;
    }

    @Override
    public List<UserEntitlementResponseDto> getUserEntitlements(Long channelId, Long userId) {
        // 1. 유저 존재 확인
        userJpaRepository.findById(userId)
            .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        // 2. 유저의 모든 상품권 조회
        List<UserEntitlement> userEntitlements = userEntitlementQueryRepository.findByUserId(
            userId);

        // 3. 유저가 속한 AUTO_ACTIVE/AUTO_ENDED 그룹 조회 (Group fetch join)
        List<UserGroup> userGroups = userGroupQueryRepository
            .findEntitlementGroupsByUserIdAndChannelId(userId, channelId);

        // 4. 각 상품권에 대한 그룹 정보 매핑
        List<UserEntitlementResponseDto> result = new ArrayList<>();

        for (UserEntitlement ue : userEntitlements) {
            Entitlement entitlement = ue.getEntitlement();

            // 유저가 속한 그룹 중 현재 entitlement와 매칭되는 그룹 찾기
            UserGroup matchedUserGroup = userGroups.stream()
                .filter(ug -> {
                    Group group = ug.getGroup();
                    return group.getEntitlement() != null
                        && group.getEntitlement().getId().equals(entitlement.getId());
                })
                .findFirst()
                .orElse(null);

            Long userGroupId = matchedUserGroup != null ? matchedUserGroup.getId() : null;
            Long groupId = matchedUserGroup != null ? matchedUserGroup.getGroup().getId() : null;

            result.add(UserEntitlementResponseDto.of(
                userId,
                userGroupId,
                groupId,
                entitlement.getName(),
                ue.getStatus(),
                ue.getStartDate(),
                ue.getEndDate()
            ));
        }

        return result;
    }

    @Override
    @Transactional
    public void processExpiredEntitlements() {
        LocalDate today = DateTimeUtils.todayKst();

        // 1. 만료된 상품권 조회
        List<UserEntitlement> expiredList = userEntitlementQueryRepository.findExpiredEntitlements(
            today);

        log.info("[만료 처리 시작] 처리 대상: {}건", expiredList.size());

        for (UserEntitlement ue : expiredList) {
            try {
                // 2. 상태를 EXPIRED로 변경
                ue.expire();

                // 3. AUTO_ACTIVE → AUTO_ENDED 그룹으로 이동
                User user = ue.getUser();
                Entitlement entitlement = ue.getEntitlement();

                // 유저가 속한 AUTO_ACTIVE 그룹 찾기 (fetch join으로 channel 포함)
                List<UserGroup> userAutoActiveGroups = userGroupQueryRepository.findByUserId(
                        user.getId())
                    .stream()
                    .filter(ug -> {
                        Group group = ug.getGroup();
                        return group.getEntitlement() != null
                            && group.getEntitlement().getId().equals(entitlement.getId())
                            && group.getGroupType() == AUTO_ACTIVE;
                    })
                    .toList();

                if (!userAutoActiveGroups.isEmpty()) {
                    // Channel 정보는 첫 번째 UserGroup의 Group에서 가져옴
                    Channel channel = userAutoActiveGroups.get(0).getGroup().getChannel();

                    // AUTO_ACTIVE 그룹에서 유저 제거
                    userAutoActiveGroups.forEach(UserGroup::deleteUserGroup);

                    // AUTO_ENDED 그룹에 유저 추가
                    Group autoEndedGroup = groupQueryRepository
                        .findByChannelAndEntitlementAndGroupType(channel, entitlement, AUTO_ENDED)
                        .orElseThrow(() -> new EntitlementException(AUTO_ENDED_GROUP_NOT_FOUND));

                    UserGroup newUserGroup = UserGroup.createUserGroup(user, autoEndedGroup);
                    userGroupJpaRepository.save(newUserGroup);

                    log.info("[만료 처리 완료] userId={}, entitlementId={}", user.getId(),
                        entitlement.getId());
                } else {
                    log.warn("[만료 처리 스킵] AUTO_ACTIVE 그룹 없음: userId={}, entitlementId={}",
                        user.getId(), entitlement.getId());
                }

            } catch (Exception e) {
                log.error("[만료 처리 실패] userEntitlementId={}, error={}", ue.getId(), e.getMessage());
            }
        }

        log.info("[만료 처리 종료] 처리 완료: {}건", expiredList.size());
    }

    /**
     * AUTO_ACTIVE → AUTO_ENDED 그룹 이동
     */
    private void moveToAutoEndedGroup(User user, Entitlement entitlement) {
        // 1. 유저가 속한 AUTO_ACTIVE 그룹 찾기
        List<UserGroup> userAutoActiveGroups = userGroupQueryRepository.findByUserId(user.getId())
            .stream()
            .filter(ug -> {
                Group group = ug.getGroup();
                return group.getEntitlement() != null
                    && group.getEntitlement().getId().equals(entitlement.getId())
                    && group.getGroupType() == AUTO_ACTIVE;
            })
            .toList();

        if (userAutoActiveGroups.isEmpty()) {
            log.warn("[그룹 이동 스킵] AUTO_ACTIVE 그룹 없음: userId={}, entitlementId={}",
                user.getId(), entitlement.getId());
            return;
        }

        // 2. Channel 정보 가져오기
        Channel channel = userAutoActiveGroups.get(0).getGroup().getChannel();

        // 3. AUTO_ACTIVE 그룹에서 유저 제거
        userAutoActiveGroups.forEach(UserGroup::deleteUserGroup);

        // 4. AUTO_ENDED 그룹 찾기 및 유저 추가
        Group autoEndedGroup = groupQueryRepository
            .findByChannelAndEntitlementAndGroupType(channel, entitlement, AUTO_ENDED)
            .orElseThrow(() -> new EntitlementException(AUTO_ENDED_GROUP_NOT_FOUND));

        UserGroup newUserGroup = UserGroup.createUserGroup(user, autoEndedGroup);
        userGroupJpaRepository.save(newUserGroup);
    }

    /**
     * AUTO_ENDED → AUTO_ACTIVE 그룹 이동
     */
    private void moveToAutoActiveGroup(User user, Entitlement entitlement) {
        // 1. 유저가 속한 AUTO_ENDED 그룹 찾기
        List<UserGroup> userAutoEndedGroups = userGroupQueryRepository.findByUserId(user.getId())
            .stream()
            .filter(ug -> {
                Group group = ug.getGroup();
                return group.getEntitlement() != null
                    && group.getEntitlement().getId().equals(entitlement.getId())
                    && group.getGroupType() == AUTO_ENDED;
            })
            .toList();

        if (userAutoEndedGroups.isEmpty()) {
            log.warn("[그룹 이동 스킵] AUTO_ENDED 그룹 없음: userId={}, entitlementId={}",
                user.getId(), entitlement.getId());
            return;
        }

        // 2. Channel 정보 가져오기
        Channel channel = userAutoEndedGroups.get(0).getGroup().getChannel();

        // 3. AUTO_ENDED 그룹에서 유저 제거
        userAutoEndedGroups.forEach(UserGroup::deleteUserGroup);

        // 4. AUTO_ACTIVE 그룹 찾기 및 유저 추가
        Group autoActiveGroup = groupQueryRepository
            .findByChannelAndEntitlementAndGroupType(channel, entitlement, AUTO_ACTIVE)
            .orElseThrow(() -> new EntitlementException(AUTO_ACTIVE_GROUP_NOT_FOUND));

        UserGroup newUserGroup = UserGroup.createUserGroup(user, autoActiveGroup);
        userGroupJpaRepository.save(newUserGroup);
    }

    @Override
    @Transactional
    public void bulkUpdateUserEntitlementDates(Long entitlementId,
        List<BulkUpdateUserEntitlementRequestDto> requests) {
        int totalUpdated = 0;
        int totalSkipped = 0;

        for (BulkUpdateUserEntitlementRequestDto request : requests) {
            int updated = userEntitlementQueryRepository.bulkUpdateDatesByPhoneNumberAndEntitlement(
                request.phoneNumber(),
                entitlementId,
                request.startDate(),
                request.endDate()
            );

            if (updated > 0) {
                totalUpdated += updated;
            } else {
                totalSkipped++;
                log.warn("[UserEntitlement 대량 업데이트 스킵] 휴대전화={}, entitlementId={}",
                    request.phoneNumber(), entitlementId);
            }
        }

        log.info("[UserEntitlement 대량 업데이트 완료] 업데이트={}, 스킵={}", totalUpdated, totalSkipped);
    }
}
