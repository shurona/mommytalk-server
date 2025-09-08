package com.shrona.mommytalk.user.presentation.controller;

import com.shrona.mommytalk.user.application.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/channels/{channelId}/users")
@RestController
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<?> findUserList(
        @PathVariable Long channelId
    ) {

        userService.findUserListByChannelInfo(channelId);

        return ResponseEntity.ok("");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> findUserById() {

        return ResponseEntity.ok("");
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUserInfo() {

        return ResponseEntity.ok("");
    }

    @PatchMapping("/{userId}/entitlements")
    public ResponseEntity<?> yahoo() {

        return ResponseEntity.ok("");
    }

}
