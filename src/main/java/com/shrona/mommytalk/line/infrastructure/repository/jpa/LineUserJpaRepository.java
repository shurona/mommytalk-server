package com.shrona.mommytalk.line.infrastructure.repository.jpa;

import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.line.infrastructure.dao.LineUserWithPhoneDao;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LineUserJpaRepository extends JpaRepository<LineUser, Long> {

    /**
     * 라인 아이디를 기준으로 라인 유저 조회
     */
    Optional<LineUser> findByLineId(String lineId);

    /**
     * 라인 유저에 필요한 정보 조회
     */
    @Query("select new com.shrona.mommytalk.line.infrastructure.dao.LineUserWithPhoneDao("
        + "lu.id, lu.lineId, u.phoneNumber) "
        + "from LineUser lu left join User u on u.lineUser = lu")
    List<LineUserWithPhoneDao> findLineUserWithPhoneNumber();
}
