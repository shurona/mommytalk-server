package com.shrona.line_demo.user.infrastructure;

import com.shrona.line_demo.user.domain.User;
import com.shrona.line_demo.user.domain.vo.PhoneNumber;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, Long> {

    //    @Query("select u from User u where u.phoneNumber in :phoneNumberList")
    List<User> findByPhoneNumberIn(List<PhoneNumber> phoneNumberList);

    Optional<User> findByPhoneNumber(PhoneNumber phoneNumber);

    Optional<User> findByPhoneNumberAndLineUserIsNull(PhoneNumber phoneNumber);

}
