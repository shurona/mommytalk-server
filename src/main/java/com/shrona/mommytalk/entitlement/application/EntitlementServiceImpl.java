package com.shrona.mommytalk.entitlement.application;

import static com.shrona.mommytalk.group.domain.GroupType.AUTO_ACTIVE;
import static com.shrona.mommytalk.group.domain.GroupType.AUTO_ENDED;

import com.shrona.mommytalk.channel.application.ChannelService;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.entitlement.domain.Entitlement;
import com.shrona.mommytalk.entitlement.infrastructure.jpa.EntitlementJpaRepository;
import com.shrona.mommytalk.entitlement.presentation.dtos.request.CreateEntitlementRequestDto;
import com.shrona.mommytalk.group.domain.Group;
import com.shrona.mommytalk.group.infrastructure.repository.jpa.GroupJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class EntitlementServiceImpl implements EntitlementService {

    private final EntitlementJpaRepository entitlementJpaRepository;
    private final GroupJpaRepository groupJpaRepository;
    private final ChannelService channelService;

    @Override
    public List<Entitlement> findAllEntitlements() {
        return entitlementJpaRepository.findAll();
    }

    @Override
    @Transactional
    public Entitlement createEntitlement(CreateEntitlementRequestDto requestDto) {
        // 1. Entitlement 생성
        Entitlement entitlement = Entitlement.createEntitlement(
            requestDto.name(),
            requestDto.type()
        );

        entitlementJpaRepository.save(entitlement);

        log.info("[상품 생성] entitlementId={}, name={}, type={}",
            entitlement.getId(), entitlement.getName(), entitlement.getType());

        // 2. 모든 채널 조회
        List<Channel> channels = channelService.findChannelList();

        log.info("[그룹 자동 생성 시작] 채널 수={}", channels.size());

        // 3. 각 채널에 AUTO_ACTIVE, AUTO_ENDED 그룹 생성
        for (Channel channel : channels) {
            // AUTO_ACTIVE 그룹 생성
            Group autoActiveGroup = Group.createEntitlementGroup(
                channel,
                entitlement,
                AUTO_ACTIVE,
                requestDto.name() + " AUTO_ACTIVE",
                requestDto.name() + " 활성"
            );
            groupJpaRepository.save(autoActiveGroup);

            // AUTO_ENDED 그룹 생성
            Group autoEndedGroup = Group.createEntitlementGroup(
                channel,
                entitlement,
                AUTO_ENDED,
                requestDto.name() + " AUTO_ENDED",
                requestDto.name() + " 비활성"
            );
            groupJpaRepository.save(autoEndedGroup);

            log.info("[그룹 생성 완료] channelId={}, autoActiveGroupId={}, autoEndedGroupId={}",
                channel.getId(), autoActiveGroup.getId(), autoEndedGroup.getId());
        }

        log.info("[상품 및 그룹 생성 완료] entitlementId={}, 생성된 그룹 수={}",
            entitlement.getId(), channels.size() * 2);

        return entitlement;
    }
}
