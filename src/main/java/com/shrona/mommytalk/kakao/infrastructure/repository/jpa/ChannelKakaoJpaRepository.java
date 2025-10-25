package com.shrona.mommytalk.kakao.infrastructure.repository.jpa;

import com.shrona.mommytalk.kakao.domain.ChannelKakaoUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelKakaoJpaRepository extends JpaRepository<ChannelKakaoUser, Long> {

}
