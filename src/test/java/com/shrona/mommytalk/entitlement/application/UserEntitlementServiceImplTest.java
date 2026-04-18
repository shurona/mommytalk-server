package com.shrona.mommytalk.entitlement.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.common.utils.DateTimeUtils;
import com.shrona.mommytalk.entitlement.domain.Entitlement;
import com.shrona.mommytalk.entitlement.domain.EntitlementType;
import com.shrona.mommytalk.entitlement.domain.UserEntitlement;
import com.shrona.mommytalk.entitlement.infrastructure.jpa.EntitlementJpaRepository;
import com.shrona.mommytalk.entitlement.infrastructure.jpa.UserEntitlementJpaRepository;
import com.shrona.mommytalk.entitlement.presentation.dtos.request.UpdateUserEntitlementRequestDto;
import com.shrona.mommytalk.line.infrastructure.repository.jpa.ChannelJpaRepository;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import com.shrona.mommytalk.user.infrastructure.repository.jpa.UserJpaRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class UserEntitlementServiceImplTest {

    @Autowired
    private UserEntitlementServiceImpl userEntitlementService;
    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private EntitlementJpaRepository entitlementJpaRepository;
    @Autowired
    private UserEntitlementJpaRepository userEntitlementJpaRepository;
    @Autowired
    private ChannelJpaRepository channelJpaRepository;

    private Long userEntitlementId;

    @BeforeEach
    void setUp() {
        Channel channel = channelJpaRepository.save(Channel.createChannel("테스트채널", "설명"));
        Entitlement entitlement = entitlementJpaRepository.save(
            Entitlement.createEntitlement("마미톡테스트", EntitlementType.MOMMYTALK));
        User user = userJpaRepository.save(
            User.createUser(new PhoneNumber("010-9000-0001")));

        LocalDate today = DateTimeUtils.todayKst();
        UserEntitlement ue = UserEntitlement.createUserEntitlement(
            user, entitlement, channel, today, today.plusDays(364));
        userEntitlementId = userEntitlementJpaRepository.save(ue).getId();
    }

    @Test
    @DisplayName("오늘 날짜로 종료일 수정 성공 (KST 기준)")
    void 오늘_날짜로_종료일_수정_성공() {
        // given
        LocalDate today = DateTimeUtils.todayKst();
        UpdateUserEntitlementRequestDto dto = new UpdateUserEntitlementRequestDto(null, today);

        // when
        UserEntitlement result = userEntitlementService.updateUserEntitlement(userEntitlementId,
            dto);

        // then
        assertThat(result.getEndDate()).isEqualTo(today);
    }
}
