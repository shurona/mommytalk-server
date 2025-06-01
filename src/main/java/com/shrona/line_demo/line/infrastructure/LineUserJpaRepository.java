package com.shrona.line_demo.line.infrastructure;

import com.shrona.line_demo.line.domain.LineUser;
import com.shrona.line_demo.user.domain.vo.PhoneNumber;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LineUserJpaRepository extends JpaRepository<LineUser, Long> {

    /**
     * 라인 아이디를 기준으로 라인 유저 조회
     */
    Optional<LineUser> findByLineId(String lineId);

    /**
     * 휴대전화가 있는 유저들 조회
     */
    List<LineUser> findByPhoneNumberIn(List<PhoneNumber> phoneNumbers);


    Optional<LineUser> findByPhoneNumber(PhoneNumber savingPhone);
}
