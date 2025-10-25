package com.shrona.mommytalk.kakao.infrastructure.repository.jpa;

import com.shrona.mommytalk.kakao.domain.KakaoUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KakaoUserJpaRepository extends JpaRepository<KakaoUser, Long> {

}
