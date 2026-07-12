package com.shrona.mommytalk.user.infrastructure;

import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.line.infrastructure.repository.jpa.LineUserJpaRepository;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import com.shrona.mommytalk.user.infrastructure.repository.jpa.UserJpaRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalTime;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class UserJpaRepositoryTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private LineUserJpaRepository lineUserJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void 번호가_있지만_라인이_빈_경우_조회() {

        // given
        PhoneNumber number = new PhoneNumber("010-3393-1234");
        LineUser lineUser = lineUserJpaRepository.save(LineUser.createLineUser("lineOne"));

        User user = userJpaRepository.save(User.createUser(number));

        // when
        Optional<User> userInfo = userJpaRepository.findByPhoneNumberAndLineUserIsNull(
            number);

        // then
        Assertions.assertThat(userInfo.isPresent()).isTrue();
    }

    @Test
    public void 번호가_있지만_라인이_이미저장되어_있는_경우() {

        // given
        PhoneNumber number = new PhoneNumber("010-3393-1234");
        LineUser lineUser = lineUserJpaRepository.save(LineUser.createLineUser("lineOne"));

        User user = userJpaRepository.save(User.createUserWithLine(number, lineUser));

        // when
        Optional<User> userInfo = userJpaRepository.findByPhoneNumberAndLineUserIsNull(
            number);

        // then
        Assertions.assertThat(userInfo.isPresent()).isFalse();
    }

    @Test
    public void 대기_선호시간_일괄_승격_테스트() {

        // given: 대기 중 2명 + 대기 없는 1명
        User pendingUser1 = userJpaRepository.save(User.createUser(new PhoneNumber("010-1111-1111")));
        User pendingUser2 = userJpaRepository.save(User.createUser(new PhoneNumber("010-2222-2222")));
        User noPendingUser = userJpaRepository.save(User.createUser(new PhoneNumber("010-3333-3333")));
        pendingUser1.updatePendingPreferredSendTime(LocalTime.of(19, 30));
        pendingUser2.updatePendingPreferredSendTime(LocalTime.of(8, 0));
        entityManager.flush();

        // when
        int promoted = userJpaRepository.promoteAllPendingPreferredSendTime();

        // then: 벌크 UPDATE 후에는 반드시 재조회로 확인 (기존 참조는 stale)
        Assertions.assertThat(promoted).isEqualTo(2);

        User found1 = userJpaRepository.findById(pendingUser1.getId()).orElseThrow();
        User found2 = userJpaRepository.findById(pendingUser2.getId()).orElseThrow();
        User found3 = userJpaRepository.findById(noPendingUser.getId()).orElseThrow();
        Assertions.assertThat(found1.getPreferredSendTime()).isEqualTo(LocalTime.of(19, 30));
        Assertions.assertThat(found1.getPendingPreferredSendTime()).isNull();
        Assertions.assertThat(found2.getPreferredSendTime()).isEqualTo(LocalTime.of(8, 0));
        Assertions.assertThat(found2.getPendingPreferredSendTime()).isNull();
        Assertions.assertThat(found3.getPreferredSendTime()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    public void 승격_중복_실행시_멱등_테스트() {

        // given
        User user = userJpaRepository.save(User.createUser(new PhoneNumber("010-4444-4444")));
        user.updatePendingPreferredSendTime(LocalTime.of(19, 30));
        entityManager.flush();
        userJpaRepository.promoteAllPendingPreferredSendTime();

        // when: 같은 날 두 번째 실행
        int secondRun = userJpaRepository.promoteAllPendingPreferredSendTime();

        // then
        Assertions.assertThat(secondRun).isZero();
        User found = userJpaRepository.findById(user.getId()).orElseThrow();
        Assertions.assertThat(found.getPreferredSendTime()).isEqualTo(LocalTime.of(19, 30));
    }

    @Test
    public void 삭제된_유저는_승격_제외_테스트() {

        // given: pending이 있지만 soft delete된 유저
        User deletedUser = userJpaRepository.save(User.createUser(new PhoneNumber("010-5555-5555")));
        deletedUser.updatePendingPreferredSendTime(LocalTime.of(19, 30));
        entityManager.flush();
        entityManager.createQuery("update User u set u.isDeleted = true where u.id = :id")
            .setParameter("id", deletedUser.getId())
            .executeUpdate();
        entityManager.clear();

        // when
        int promoted = userJpaRepository.promoteAllPendingPreferredSendTime();

        // then
        Assertions.assertThat(promoted).isZero();
    }

}