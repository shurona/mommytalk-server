package com.shrona.mommytalk.kakao.domain;

import com.shrona.mommytalk.common.entity.BaseEntity;
import com.shrona.mommytalk.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "kakao_user")
public class KakaoUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "kakao_id", unique = true)
    private String kakaoId;

    @OneToOne(mappedBy = "kakaoUser", fetch = FetchType.LAZY)
    private User user;

    public static KakaoUser createKakaoUser(String kakaoId, User userInfo) {
        KakaoUser kakaoUser = new KakaoUser();
        kakaoUser.kakaoId = kakaoId;
        kakaoUser.user = userInfo;
        return kakaoUser;
    }

    /**
     * 처음으로 카카오 아이디를 지정해준다.
     */
    public void setFirstKakaoId(String kakaoId) {
        if (StringUtils.isEmpty(this.kakaoId) || this.kakaoId.startsWith("temp")) {
            this.kakaoId = kakaoId;
        }
    }

}
