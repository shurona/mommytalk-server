package com.shrona.line_demo.user.infrastructure;

import com.shrona.line_demo.line.domain.Channel;
import com.shrona.line_demo.user.domain.Group;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GroupJpaRepository extends JpaRepository<Group, Long> {

    /**
     * User까지 fetch join 해서 그룹 정보 갖고 오기
     */
    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.userGroupList ug LEFT JOIN FETCH ug.user WHERE g.id = :id")
    Optional<Group> findGroupWithUsers(Long id);

    /**
     * 그룹 아이디가 없는 그룹 목록 조회
     */
    List<Group> findByChannelAndIdNotIn(Channel channel, List<Long> exceptGroupIds);

    /**
     * 페이징 없이 모든 그룹 목록 조회
     */
    List<Group> findAllByChannel(Channel channel);

    /**
     * 채널에 속한 그룹 목록 조회
     */
    Page<Group> findAllByChannel(Channel channel, Pageable pageable);

    /**
     * 그룹 아이디를 기준으로 속한 라인 아이디를 조사한다.
     */
    @Query("""
        SELECT DISTINCT ug.user.lineUser.lineId
        FROM UserGroup ug
        WHERE ug.group.id IN :groupIds
        AND ug.user.lineUser IS NOT NULL
        """)
    List<String> findLineIdsByGroupIds(List<Long> groupIds);

}
