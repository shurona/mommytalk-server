package com.shrona.mommytalk.group.infrastructure.repository;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.group.domain.Group;
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
    
}
