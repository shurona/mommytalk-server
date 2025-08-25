package com.shrona.line_demo.line.infrastructure;

import com.shrona.line_demo.line.domain.LineUser;
import com.shrona.line_demo.line.infrastructure.dao.LineUserWithPhoneDao;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LineUserJpaRepository extends JpaRepository<LineUser, Long> {

    /**
     * 라인 아이디를 기준으로 라인 유저 조회
     */
    Optional<LineUser> findByLineId(String lineId);

    //
    @Query("select new com.shrona.line_demo.line.infrastructure.dao.LineUserWithPhoneDao("
        + "lu.id, lu.lineId, u.phoneNumber) "
        + "from LineUser lu left join User u on u.lineUser = lu")
    List<LineUserWithPhoneDao> findLineUserWithPhoneNumber();
}
