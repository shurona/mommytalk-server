package com.shrona.line_demo.line.application;

import static com.shrona.line_demo.line.common.exception.LineErrorCode.BAD_REQUEST;

import com.shrona.line_demo.common.core.PhoneProcess;
import com.shrona.line_demo.line.common.exception.LineException;
import com.shrona.line_demo.line.domain.LineMessage;
import com.shrona.line_demo.line.domain.LineUser;
import com.shrona.line_demo.line.infrastructure.LineMessageJpaRepository;
import com.shrona.line_demo.line.infrastructure.LineUserJpaRepository;
import com.shrona.line_demo.user.domain.User;
import com.shrona.line_demo.user.domain.vo.PhoneNumber;
import com.shrona.line_demo.user.infrastructure.UserJpaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class LineServiceImpl implements LineService {

    private static final String numberRegex = "\\d{3}([- ])\\d{4}\\1\\d{4}";

    // repository
    private final LineUserJpaRepository lineUserRepository;
    private final LineMessageJpaRepository lineMessageRepository;
    private final UserJpaRepository userRepository;

    private final PhoneProcess phoneProcess;


    @Override
    public LineUser findLineUserByLineId(String lineId) {
        return findLineUserByLineIdWithException(lineId);
    }

    @Override
    public Page<LineUser> findLineUserList(Pageable pageable) {
        return lineUserRepository.findAll(pageable);
    }

    //
    @Transactional
    public void saveLineMessage(String lineId, String content) {
        Optional<LineUser> lineUserByLineId = lineUserRepository.findByLineId(lineId);

        LineUser lineUser;
        // 비어있으면 새로 만들어 만들어준다.
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

        // 휴대전화 번호 형식인지 확인 후 로직 처리
        validatePhoneAndMatchUser(content, lineUser);
    }

    public void validatePhoneAndMatchUser(String content, LineUser lineUser) {
        if (phoneProcess.isValidFormat(content)) {
            // 번호 저장
            lineUser.settingPhoneNumber(new PhoneNumber(content));

            // 유저가 존재하는 지 확인하고 있으면 라인 아이디와 정보를 넣어준다.
            Optional<User> userInfo = userRepository.findByPhoneNumberAndLineUserIsNull(
                new PhoneNumber(content));

            // 라인 유저가 비어 있으면 매칭시켜준다.
            userInfo.ifPresent(user -> user.matchUserWithLine(lineUser));
        }
    }

    @Transactional
    public LineUser followLineUserByLineId(String lineId) {

        Optional<LineUser> lineUserByLineId = lineUserRepository.findByLineId(lineId);

        // 비어있으면 새로 만들어 만들어준다.
        if (lineUserByLineId.isEmpty()) {
            LineUser lineUser = LineUser.createLineUser(lineId);
            return lineUserRepository.save(lineUser);
        } else {
            lineUserByLineId.get().changeFollowStatus(true);
        }

        return lineUserByLineId.get();
    }

    @Transactional
    public void unfollowLineUserByLineId(String lineId) {

        Optional<LineUser> lineUserInfo = lineUserRepository.findByLineId(lineId);

        // 있으면 follow를 해제로 변경해준다.
        lineUserInfo.ifPresent(lineUser -> lineUser.changeFollowStatus(false));
    }

    private LineUser findLineUserByLineIdWithException(String lineId) {
        return lineUserRepository.findByLineId(lineId).orElseThrow(
            () -> new LineException(BAD_REQUEST)
        );
    }

}
