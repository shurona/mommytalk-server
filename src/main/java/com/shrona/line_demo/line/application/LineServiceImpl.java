package com.shrona.line_demo.line.application;

import static com.shrona.line_demo.common.exception.LineErrorCode.BAD_REQUEST;

import com.shrona.line_demo.common.exception.LineException;
import com.shrona.line_demo.line.domain.LineMessage;
import com.shrona.line_demo.line.domain.LineUser;
import com.shrona.line_demo.line.infrastructure.LineMessageJpaRepository;
import com.shrona.line_demo.line.infrastructure.LineUserJpaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class LineServiceImpl implements LineService {

    private static final String numberRegex = "\\d{3}([- ])\\d{4}\\1\\d{4}";

    private final LineUserJpaRepository lineUserRepository;
    private final LineMessageJpaRepository lineMessageRepository;

    /*
        휴대전화 입력 검증
     */
    public boolean isValidFormat(String input) {
        String regex = "\\d{3}([- ])\\d{3,4}\\1\\d{4}";
        return input.matches(regex);
    }

    //
    @Transactional
    public void saveLineMessage(String lineId, String content) {
        Optional<LineUser> lineUserByLineId = lineUserRepository.findByLineId(lineId);

        LineUser lineUser;
        if (lineUserByLineId.isEmpty()) {
            LineUser newLineUser = LineUser.createLineUser(lineId);
            lineUser = lineUserRepository.save(newLineUser);
        } else {
            lineUser = lineUserByLineId.get();
        }

        // 메시지 저장
        lineMessageRepository.save(LineMessage.createLineMessage(lineUser, content));

        // 이미 구매한 유저면 더 이상의 로직 처리는 하지 않는다.
        if (lineUser.isPurchased()) {
            return;
        }

        // 휴대전화 번호 형식이면 번호를 저장해준다.
        if (isValidFormat(content)) {
            lineUser.settingPhoneNumber(content);
        }
    }

    @Transactional
    public void followLineUserByLineId(String lineId) {

        Optional<LineUser> lineUserByLineId = lineUserRepository.findByLineId(lineId);

        if (lineUserByLineId.isEmpty()) {
            LineUser lineUser = LineUser.createLineUser(lineId);
            lineUserRepository.save(lineUser);
        } else {
            lineUserByLineId.get().changeFollowStatus(true);
        }
    }

    @Transactional
    public void unfollowLineUserByLineId(String lineId) {

        Optional<LineUser> lineById = lineUserRepository.findByLineId(lineId);
        LineUser lineUserByLineId = findLineUserByLineId(lineId);
        lineUserByLineId.changeFollowStatus(false);
    }

    private LineUser findLineUserByLineId(String lineId) {
        return lineUserRepository.findByLineId(lineId).orElseThrow(
            () -> new LineException(BAD_REQUEST)
        );
    }

}
