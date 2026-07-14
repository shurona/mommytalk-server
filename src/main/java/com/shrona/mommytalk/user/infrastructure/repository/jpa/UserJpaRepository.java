package com.shrona.mommytalk.user.infrastructure.repository.jpa;

import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserJpaRepository extends JpaRepository<User, Long> {

    /**
     * 대기 중인 선호 발송 시간을 일괄 승격한다. (current ← pending, pending ← NULL)
     * SQLRestriction(soft delete)은 벌크 UPDATE에 적용되지 않으므로 isDeleted 조건을 직접 건다.
     */
    @Modifying(clearAutomatically = true)
    @Query("update User u set u.preferredSendTime = u.pendingPreferredSendTime, "
        + "u.pendingPreferredSendTime = null "
        + "where u.pendingPreferredSendTime is not null and u.isDeleted = false")
    int promoteAllPendingPreferredSendTime();

    //    @Query("select u from User u where u.phoneNumber in :phoneNumberList")
    List<User> findByPhoneNumberIn(List<PhoneNumber> phoneNumberList);

    Optional<User> findByPhoneNumber(PhoneNumber phoneNumber);

    /**
     * 라인 유저가 비어있으면서 휴대전화와 매칭되는 유저를 갖고 온다.
     */
    Optional<User> findByPhoneNumberAndLineUserIsNull(PhoneNumber phoneNumber);

    Optional<User> findByLineUser(LineUser lineUserInfo);
}
