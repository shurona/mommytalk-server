package com.shrona.mommytalk.line.presentation.form;

import com.shrona.mommytalk.line.infrastructure.dao.ChannelLineUserWithPhoneDao;
import java.time.LocalDateTime;

public record LineUserForm(
    Long id,
    String lineId,
    String phoneNumber,
    LocalDateTime joinDate
) {

    public static LineUserForm of(ChannelLineUserWithPhoneDao lineUserWithPhoneDao) {
        //TODO: 휴대전화 선택 로직 수정
        String phone =
            lineUserWithPhoneDao.phoneNumber() != null
                ? lineUserWithPhoneDao.phoneNumber().getPhoneNumber() : "";
        return new LineUserForm(
            lineUserWithPhoneDao.lineSeq(),
            lineUserWithPhoneDao.lineId(),
            phone,
            lineUserWithPhoneDao
                .createdAt().plusHours(9)); // TODO: 서버는 utc 사용하고 클라이언트에서 반영하도록 변경
    }

}
