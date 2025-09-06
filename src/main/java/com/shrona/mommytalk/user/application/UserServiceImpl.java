package com.shrona.mommytalk.user.application;

import static com.shrona.mommytalk.user.common.exception.UserErrorCode.DUPLICATE_PHONE_NUMBER;

import com.shrona.mommytalk.common.utils.PhoneProcess;
import com.shrona.mommytalk.line.common.exception.LineErrorCode;
import com.shrona.mommytalk.line.common.exception.LineException;
import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.line.infrastructure.LineUserJpaRepository;
import com.shrona.mommytalk.user.common.exception.UserException;
import com.shrona.mommytalk.user.common.utils.UserUtils;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import com.shrona.mommytalk.user.infrastructure.UserGroupJpaRepository;
import com.shrona.mommytalk.user.infrastructure.UserJpaRepository;
import com.shrona.mommytalk.user.infrastructure.repository.UserQueryRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    // jpa
    private final LineUserJpaRepository lineUserJpaRepository;

    private final UserJpaRepository userRepository;
    private final UserQueryRepository userQueryRepository;

    private final UserGroupJpaRepository userGroupRepository;

    // 휴대폰 관련 process 처리
    private final PhoneProcess phoneProcess;
    private final UserUtils userUtils;

    @Transactional
    public User createUser(String phoneNumber) {

        PhoneNumber number = new PhoneNumber(phoneNumber);

        Optional<User> userInfo = userRepository.findByPhoneNumber(
            number);

        // 있으면 이미 있는 유저를 전달한다.
        if (userInfo.isPresent()) {
            return userInfo.get();
        }

        User user = User.createUser(number);
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User findUserByPhoneNumber(String phoneNumber) {
        PhoneNumber number = new PhoneNumber(phoneNumber);
        Optional<User> userInfo = userRepository.findByPhoneNumber(number);

        return userInfo.orElse(null);
    }

    @Override
    public Optional<User> findUserByLineUser(LineUser lineUser) {
        return userRepository.findByLineUser(lineUser);
    }

    @Override
    public List<User> findUserList() {
        return userRepository.findAll();
    }

    @Transactional
    public List<User> findOrCreateUsersWithLinesByPhoneNumbers(List<String> phoneNumberList) {

        // 중복제거
        List<String> removeDupNumber = new ArrayList<>(new HashSet<>(phoneNumberList));

        // 전화번호 변환 및 필터링
        List<PhoneNumber> userPhoneList = phoneProcess.validateAndConvertPhoneNumbers(
            removeDupNumber);

        // 기존 사용자 조회
        List<User> existingUsers = userRepository.findByPhoneNumberIn(userPhoneList);

        // 기존 사용자들의 전화번호를 추출한다.
        Set<PhoneNumber> existingPhones = userUtils.extractPhoneNumbers(existingUsers);

        // 입력받은 휴대전화 목록 중에서 등록되지 않은 전화 번호 목록을 추출
        List<PhoneNumber> notFoundNumbers = findNotFoundPhoneNumbers(userPhoneList, existingPhones);

        // 없는 전화번호인 경우 새로운 유저로 생성해준다.
        List<User> newUsers = userRepository.saveAll(
            notFoundNumbers.stream().map(User::createUser).toList());

        // 두 List를 합쳐서 반환
        return combineUsers(newUsers, existingUsers);
    }

    @Transactional
    public void updateUserPhoneNumberByLineUser(String lineId, String phoneNumber) {

        // 휴대전화가 중복인 지 검사한다.
        PhoneNumber phone = checkExistPhoneNumber(phoneNumber);

        // Line 아이디를 갖고 있는 유저를 조회 한다.
        User userInfo = userQueryRepository.findUserByLineId(lineId);

        LineUser lineUserInfo = lineUserJpaRepository.findByLineId(lineId)
            .orElseThrow(() -> new LineException(
                LineErrorCode.LINEUSER_NOT_FOUND));

        // 유저가 없으면 유저에 휴대전화를 추가해서 생성해준다.
        if (userInfo == null) {
            userRepository.save(User.createUserWithLine(phone, lineUserInfo));
        } else {
            // 유저가 있으면 유저의 휴대전화를 변경해준다.
            userInfo.updatePhoneNumber(phone);
        }
    }

    @Transactional
    public void deleteUser(User user) {
        user.deleteUser();
        userRepository.flush();
    }

    @Transactional
    public void deleteUserGroupAndUserInfo(String phoneNumber) {
        Optional<User> userInfo = userRepository.findByPhoneNumber(
            PhoneNumber.changeWithoutError(phoneNumber));

        if (userInfo.isPresent()) {
            User user = userInfo.get();
            // SQLRestriction을 무시하고 삭제
            userGroupRepository.deleteAllByUserIdWithoutRestriction(user.getId());

            // 그 다음 User 삭제
            userRepository.delete(user);
        }
    }

    /**
     * 새로 생긴 유저와 존재하는 유저 목록을 하나로 합쳐서 List로 반환한다.
     */
    private List<User> combineUsers(List<User> existing, List<User> newUsers) {
        return Stream.concat(existing.stream(), newUsers.stream())
            .toList();
    }


    /**
     * foundPhoneNumbers 존재하지 않는 휴대전화 목록을 반환한다.
     */
    private List<PhoneNumber> findNotFoundPhoneNumbers(
        List<PhoneNumber> userPhoneList,
        Set<PhoneNumber> foundPhoneNumbers
    ) {
        return userPhoneList.stream()
            .filter(phone -> !foundPhoneNumbers.contains(phone))
            .toList();
    }

    /**
     * 휴대전화 번호를 갖고 있는 유저가 존재하는 지 확인한다.
     */
    private PhoneNumber checkExistPhoneNumber(String phoneNumber) {
        PhoneNumber phone = PhoneNumber.changeWithoutError(phoneNumber);
        userRepository.findByPhoneNumber(phone)
            .ifPresent(user -> {
                throw new UserException(DUPLICATE_PHONE_NUMBER);
            });

        return phone;
    }
}
