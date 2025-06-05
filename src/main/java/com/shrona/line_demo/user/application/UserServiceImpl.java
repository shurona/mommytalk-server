package com.shrona.line_demo.user.application;

import com.shrona.line_demo.common.utils.PhoneProcess;
import com.shrona.line_demo.line.domain.LineUser;
import com.shrona.line_demo.line.infrastructure.LineUserJpaRepository;
import com.shrona.line_demo.user.common.utils.UserUtils;
import com.shrona.line_demo.user.domain.User;
import com.shrona.line_demo.user.domain.vo.PhoneNumber;
import com.shrona.line_demo.user.infrastructure.UserJpaRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    private final UserJpaRepository userRepository;
    private final LineUserJpaRepository lineUserRepository;

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
    public User findUserByPhoneNumber(String phoneNumber) {
        PhoneNumber number = new PhoneNumber(phoneNumber);
        Optional<User> userInfo = userRepository.findByPhoneNumber(number);

        return userInfo.orElse(null);
    }

    @Override
    public List<User> findUserList() {
        return userRepository.findAll();
    }

    @Transactional
    public List<User> findOrCreateUsersByPhoneNumbers(List<String> phoneNumberList) {

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

        // 등록되지 않은 휴대전화 번호 목록으로 lineUser 목록을 조회한다.
        Map<PhoneNumber, LineUser> phoneToLineUser = createPhoneToLineUserMap(notFoundNumbers);

        // 새로운 유저 목록을 라인이 있는 유저와 없는 유저를 구분해서 생성해준다.
        List<User> newUsers = userRepository.saveAll(
            createNewUsers(notFoundNumbers, phoneToLineUser)
        );

        // 두 List를 합쳐서 반환
        return combineUsers(newUsers, existingUsers);
    }

    @Transactional
    public void deleteUser(User user) {
        user.deleteUser();
        userRepository.flush();
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
     * [휴대전화 : 라인 유저] 형식으로 매핑해준다.
     */
    private Map<PhoneNumber, LineUser> createPhoneToLineUserMap(List<PhoneNumber> notFoundNumbers) {
        List<LineUser> lineUsers = lineUserRepository.findByPhoneNumberIn(notFoundNumbers);
        return lineUsers.stream()
            .collect(Collectors.toMap(
                LineUser::getPhoneNumber,
                Function.identity()
            ));
    }

    /**
     * 라인이 있는 유저와 없는 유저와 구분해서 User 객체 생성
     */
    private List<User> createNewUsers(
        List<PhoneNumber> notFoundNumbers,
        Map<PhoneNumber, LineUser> phoneToLineUser
    ) {
        return notFoundNumbers.stream()
            .map(phone -> phoneToLineUser.containsKey(phone)
                ? User.createUserWithLine(phone, phoneToLineUser.get(phone))
                : User.createUser(phone))
            .toList();
    }
}
